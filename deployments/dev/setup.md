### One-Time Server Setup

0.  **Install Podman:** Ensure `podman` is installed on the VPS.

1.  **Create Deploy User & Directories:**
    ```bash
    # As root or with sudo
    useradd -m -s /bin/bash geohod_backend_dev
    mkdir -p /home/geohod_backend_dev/geohod-backend-dev
    chown -R geohod_backend_dev:geohod_backend_dev /home/geohod_backend_dev/geohod-backend-dev
    ```

2.  **Enable User Lingering:** This crucial step allows the user's `systemd` services to run even when the user is not logged in. This command must be run as root once.
    ```bash
    # As root or with sudo
    loginctl enable-linger geohod_backend_dev
    ```

3.  **SSH Access:** Add the public key corresponding to the `VPS_SSH_KEY` GitHub secret to the `~/.ssh/authorized_keys` file for the `geohod_backend_dev` user.

### Running a Deployment

1.  Ensure all required secrets (`VPS_USER`, `VPS_SSH_KEY`, etc.) and variables (`VPS_HOST`) are configured in the GitHub repository settings under **Settings > Secrets and variables > Actions**.
2.  Go to the **Actions** tab in the GitHub repository.
3.  Select the **Deploy to Dev** workflow from the list.
4.  Click the **Run workflow** button to trigger a manual deployment.

### Postgres One-Time Server Setup

1. Create `~/.config/geohod/postgres-dev.env`

**Service** `.config/systemd/user/container-geohod-postgres-dev.service`
```ini
[Unit]
Description=Geohod Postgres container (dev)
After=network.target

[Service]
Restart=on-failure
RestartSec=5
ExecStartPre=mkdir -p %h/geohod-backend-dev/postgres_data
ExecStartPre=/usr/bin/podman pull docker.io/library/postgres:17-alpine
EnvironmentFile=%h/.config/geohod/postgres-dev.env
ExecStart=/usr/bin/podman run -d --name geohod-postgres-dev --network geohod-net \
  -e POSTGRES_DB=${POSTGRES_DB} \
  -e POSTGRES_USER=${POSTGRES_USER} \
  -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
  -v %h/geohod-backend-dev/postgres_data:/var/lib/postgresql/data \
  docker.io/library/postgres:17-alpine
ExecStop=/usr/bin/podman stop geohod-postgres-dev
TimeoutStartSec=120

[Install]
WantedBy=default.target
```