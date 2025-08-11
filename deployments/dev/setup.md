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
After=network-online.target
Wants=network-online.target

[Service]
Type=exec
Restart=on-failure
RestartSec=5s
TimeoutStartSec=120s

ExecStart=/usr/bin/podman run --name geohod-postgres-dev \
  --network geohod-dev-net \
  --rm \
  --mount type=volume,source=geohod-postgres-dev_data,destination=/var/lib/postgresql/data \
  --env-file=%h/.config/geohod/postgres-dev.env \
  docker.io/library/postgres:17-alpine

[Install]
WantedBy=default.target
```