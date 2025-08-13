# V2 Deployment Setup (Podman Compose)

## One-Time Server Setup
- **Install Podman & Podman Compose**
  - Follow official documentation for your OS.
- **Create Deploy User & Directories**
  ```bash
  # As root or with sudo
  sudo useradd -m -s /bin/bash geohod
  sudo mkdir -p /home/geohod/geohod-backend-dev/v2
  sudo chown -R geohod:geohod /home/geohod
  ```
- **Enable User Lingering**
  ```bash
  # As root or with sudo
  sudo loginctl enable-linger geohod
  ```
- **SSH Access**
  - Add the public key for the `VPS_SSH_KEY` secret to `~/.ssh/authorized_keys` for the `geohod` user.

## Running a Deployment
- **Configure GitHub Secrets & Variables**
  - `VPS_HOST`, `VPS_USER`, `VPS_SSH_KEY`
  - `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
  - `TELEGRAM_BOT_TOKEN`, `TELEGRAM_BOT_USERNAME`
  - etc.
- **Run GitHub Action**
  - Go to **Actions** -> **Deploy dev (v2)** -> **Run workflow**. The first run will set up and start the systemd service automatically.