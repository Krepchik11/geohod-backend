# Geohod Backend - Dev Deployment Plan

This document outlines the files required to implement the CI/CD pipeline for the dev environment.

## 1. GitHub Actions Workflow (`.github/workflows/deploy-dev.yml`)

This workflow automates the build, packaging, and deployment of the application to the development VPS. It is triggered manually.

```yaml
name: Deploy to Dev

on:
  workflow_dispatch:

jobs:
  build_and_deploy:
    name: Build and Deploy
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v5

      - name: Set up JDK 23
        uses: actions/setup-java@v5
        with:
          java-version: '23'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4.4.2

      - name: Build with Gradle
        run: ./gradlew build -x test

      - name: Get short commit SHA
        id: vars
        run: echo "sha_short=$(git rev-parse --short HEAD)" >> $GITHUB_OUTPUT

      - name: Set up Podman
        run: |
          sudo apt-get update
          sudo apt-get install -y podman

      - name: Build Podman image
        run: |
          podman build \
            -t geohod-backend:dev-${{ steps.vars.outputs.sha_short }} \
            -t geohod-backend:dev-latest \
            -f ./.github/Dockerfile .

      - name: Save Podman image to tarball
        run: |
          podman save -o geohod-backend-dev-${{ steps.vars.outputs.sha_short }}.tar.gz geohod-backend:dev-${{ steps.vars.outputs.sha_short }}

      - name: Render environment file
        run: |
          cat << EOF > geohod-dev.env
          POSTGRES_DB=${{ secrets.POSTGRES_DB }}
          POSTGRES_USER=${{ secrets.POSTGRES_USER }}
          POSTGRES_PASSWORD=${{ secrets.POSTGRES_PASSWORD }}
          TELEGRAM_BOT_TOKEN=${{ secrets.TELEGRAM_BOT_TOKEN }}
          TELEGRAM_BOT_USERNAME=${{ secrets.TELEGRAM_BOT_USERNAME }}
          CORS_ALLOWED_ORIGINS=${{ vars.CORS_ALLOWED_ORIGINS }}
          GEOHOD_CREATED_EVENT_LINK_TEMPLATE=${{ vars.GEOHOD_CREATED_EVENT_LINK_TEMPLATE }}
          GEOHOD_REVIEW_LINK_TEMPLATE=${{ vars.GEOHOD_REVIEW_LINK_TEMPLATE }}
          GEOHOD_IMAGE_TAG=dev-${{ steps.vars.outputs.sha_short }}
          EOF

      - name: Create deployment artifact archive
        run: |
          mkdir staging
          mv geohod-backend-dev-${{ steps.vars.outputs.sha_short }}.tar.gz staging/
          mv geohod-dev.env staging/
          # This assumes the service file is in deployments/dev/
          cp deployments/dev/* staging/
          tar -czvf geohod-deploy-artifacts-${{ steps.vars.outputs.sha_short }}.tar.gz staging

      - name: Transfer artifact to VPS
        uses: appleboy/scp-action@v1.0.0
        with:
          host: ${{ vars.VPS_HOST }}
          username: ${{ secrets.VPS_USER }}
          key: ${{ secrets.VPS_SSH_KEY }}
          source: "geohod-deploy-artifacts-*.tar.gz"
          target: "~/geohod-backend-dev"

      - name: Execute remote deployment
        uses: appleboy/ssh-action@v1.0.0
        with:
          host: ${{ vars.VPS_HOST }}
          username: ${{ secrets.VPS_USER }}
          key: ${{ secrets.VPS_SSH_KEY }}
          script: |
            set -e
            # Unpack the archive in the user's home directory
            tar -xzvf ~/geohod-backend-dev/geohod-deploy-artifacts-*.tar.gz -C ~/geohod-backend-dev/
            # Run the installer script with sudo
            sudo /home/${{ secrets.VPS_USER }}/geohod-backend-dev/staging/install.sh
```

## 2. Installer Script (`deployments/dev/install.sh`)

This script will be executed on the VPS to perform the deployment. It handles setting up the environment, loading the new image from a tarball, and restarting the services.

```bash
#!/bin/bash
set -euo pipefail

# This script must be run as root via sudo.
if [ "$(id -u)" -ne 0 ]; then
  echo "This script requires superuser privileges. Please run with sudo." >&2
  exit 1
fi

# Determine the home directory of the user who invoked sudo
SUDO_USER_HOME=$(getent passwd "${SUDO_USER}" | cut -d: -f6)
STAGING_DIR="${SUDO_USER_HOME}/geohod-backend-dev/staging"
CONFIG_DIR="/etc/geohod"
SERVICE_NAME="geohod-backend-dev"
ENV_FILE="geohod-dev.env"

echo "--- Starting Geohod Backend Dev Deployment ---"

# 1. Validate required files exist in staging area
echo "--> Validating artifacts in ${STAGING_DIR}..."
if [ ! -f "${STAGING_DIR}/${ENV_FILE}" ] || \
   [ ! -f "${STAGING_DIR}/${SERVICE_NAME}.service" ] || \
   ! ls "${STAGING_DIR}"/geohod-backend-dev-*.tar.gz 1> /dev/null 2>&1; then
    echo "Error: Missing required deployment files in ${STAGING_DIR}." >&2
    exit 1
fi
echo "Artifacts validated."

# 2. Setup configuration
echo "--> Updating environment configuration..."
mkdir -p "${CONFIG_DIR}"
cp "${STAGING_DIR}/${ENV_FILE}" "${CONFIG_DIR}/${ENV_FILE}"
chown root:root "${CONFIG_DIR}/${ENV_FILE}"
chmod 640 "${CONFIG_DIR}/${ENV_FILE}"
echo "Environment file placed at ${CONFIG_DIR}/${ENV_FILE}"

# 3. Load the new image from tarball
source "${CONFIG_DIR}/${ENV_FILE}"
IMAGE_TARBALL="${STAGING_DIR}/geohod-backend-dev-${GEOHOD_IMAGE_TAG}.tar.gz"

echo "--> Loading image from ${IMAGE_TARBALL}..."
if ! podman load -i "${IMAGE_TARBALL}"; then
    echo "Error: Failed to load image from ${IMAGE_TARBALL}" >&2
    exit 1
fi
echo "Image loaded successfully."

# 4. Update and enable systemd service
echo "--> Updating systemd service..."
cp "${STAGING_DIR}/${SERVICE_NAME}.service" "/etc/systemd/system/${SERVICE_NAME}.service"
chown root:root "/etc/systemd/system/${SERVICE_NAME}.service"
chmod 644 "/etc/systemd/system/${SERVICE_NAME}.service"

echo "--> Reloading systemd daemon and restarting service..."
systemctl daemon-reload
systemctl enable "${SERVICE_NAME}"
systemctl restart "${SERVICE_NAME}"

# 5. Clean up old images
echo "--> Cleaning up old images..."
podman image prune -f --filter "label=stage=dev" --filter "until=24h"

echo "--- Geohod Backend dev deployment finished successfully ---"
exit 0
```

## 3. Systemd Service File (`deployments/dev/geohod-backend-dev.service`)

This service file defines how to run the application as a system service using Podman. It does **not** pull the image, as the installer script is responsible for loading it locally.

```ini
[Unit]
Description=Geohod Backend (Dev)
Wants=network-online.target
After=network-online.target

[Service]
Restart=always
TimeoutStartSec=5min

# Load environment variables from a dedicated config file
EnvironmentFile=/etc/geohod/geohod-dev.env

# Stop and remove any existing container with the same name on service stop.
ExecStop=/usr/bin/podman stop -t 10 %n
ExecStopPost=/usr/bin/podman rm -f %n

# Start the container. The image is expected to be available locally.
# The GEOHOD_IMAGE_TAG var is loaded from the EnvironmentFile, defining which image tag to use.
ExecStart=/usr/bin/podman run --rm --name %n \
  --env-file=/etc/geohod/geohod-dev.env \
  --publish 8081:8080 \
  --label stage=dev \
  geohod-backend:${GEOHOD_IMAGE_TAG}

[Install]
WantedBy=multi-user.target
```

## 4. Server Setup & Deployment Documentation

This section provides brief instructions for setting up the target VPS and running the deployment.

### One-Time Server Setup

1.  **Install Podman:** Ensure `podman` is installed on the VPS.
    ```bash
    sudo apt-get update && sudo apt-get install -y podman
    ```

2.  **Create Deploy User & Directories:** (Replace `vps-user` with your actual deploy username)
    ```bash
    # As root or with sudo
    useradd -m -s /bin/bash vps-user
    mkdir -p /home/vps-user/geohod-backend-dev
    chown -R vps-user:vps-user /home/vps-user/geohod-backend-dev
    ```

3.  **Configure Sudo:** The deploy user needs passwordless `sudo` access **only** for the installer script. Run `sudo visudo` and add the following line, replacing `vps-user` with the correct username:

    ```
    vps-user ALL=(root) NOPASSWD: /home/vps-user/geohod-backend-dev/staging/install.sh
    ```

4.  **SSH Access:** Add the public key corresponding to the `VPS_SSH_KEY` GitHub secret to the `~/.ssh/authorized_keys` file for the `vps-user`.

### Running a Deployment

1.  Ensure all required secrets (`VPS_USER`, `VPS_SSH_KEY`, etc.) and variables (`VPS_HOST`) are configured in the GitHub repository settings under **Settings > Secrets and variables > Actions**.
2.  Go to the **Actions** tab in the GitHub repository.
3.  Select the **Deploy to Dev** workflow from the list.
4.  Click the **Run workflow** button to trigger a manual deployment.