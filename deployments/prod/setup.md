### One-Time Server Setup

0.  **Install Podman:** Ensure `podman` is installed on the VPS.

1.  **Create Deploy User & Directories:**
    ```bash
    # As root or with sudo
    useradd -m -s /bin/bash geohod_backend
    mkdir -p /home/geohod_backend/geohod-backend-prod
    chown -R geohod_backend:geohod_backend /home/geohod_backend/geohod-backend-prod
    ```

2.  **Enable User Lingering:** This crucial step allows the user's `systemd` services to run even when the user is not logged in. This command must be run as root once.
    ```bash
    # As root or with sudo
    loginctl enable-linger geohod_backend
    ```

3.  **SSH Access:** Add the public key corresponding to the `VPS_SSH_KEY` GitHub secret to the `~/.ssh/authorized_keys` file for the `geohod_backend` user.

### Running a Deployment

1.  Ensure all required secrets (`VPS_USER`, `VPS_SSH_KEY`, etc.) and variables (`VPS_HOST`) are configured in the GitHub repository settings under **Settings > Secrets and variables > Actions**.
2.  Go to the **Actions** tab in the GitHub repository.
3.  Select the **Deploy to prod** workflow from the list.
4.  Click the **Run workflow** button to trigger a manual deployment.

### Postgres One-Time Server Setup

1. Create `epostgres-prod.env`

**Service** `.config/systemd/user/container-geohod-postgres-prod.service`
```ini
[Unit]
Description=Geohod Postgres container (prod)
After=network-online.target
Wants=network-online.target

[Service]
Type=exec
Restart=on-failure
RestartSec=5s
TimeoutStartSec=120s

ExecStartPre=/usr/bin/podman network inspect geohod-prod-net >/dev/null || /usr/bin/podman network create geohod-prod-net

ExecStart=/usr/bin/podman run --name geohod-postgres-prod \
  --network geohod-prod-net \
  --rm \
  --mount type=volume,source=geohod-postgres-prod_data,destination=/var/lib/postgresql/data \
  --env-file=%h/.config/geohod/postgres-prod.env \
  docker.io/library/postgres:17-alpine

[Install]
WantedBy=default.target
```