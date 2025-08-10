# Deploy (extremely short)

Quick steps to deploy Geohod backend (dev).

Preconditions
- Podman installed on host.
- Admin added narrow sudoers entry for the deploy user.
- CI has these GitHub Secrets: POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD, TELEGRAM_BOT_TOKEN, TELEGRAM_BOT_USERNAME.

One-time admin
- Add via visudo:
  deployuser ALL=(root) NOPASSWD: /usr/local/bin/geohod-deploy-systemd.sh

CI deploy flow
- CI asserts required secrets early and checks sudoers on host.
- CI builds image, saves tarball.
- CI renders a temporary env from Secrets, SCPs the tarball + unit files + env + healthcheck to the host.
- CI deletes the temp env on the runner.
- CI SSHs to host, stages files and runs:
  sudo /usr/local/bin/geohod-deploy-systemd.sh install /tmp/geohod-deploy-staging

Quick verify
- systemctl is-active container-geohod-backend-dev.service
- curl --fail http://localhost:8081/actuator/health
- sudo ls -l /etc/geohod

Key files
- [`deployments/dev/geohod-deploy-systemd.sh`](deployments/dev/geohod-deploy-systemd.sh:1)
- [`deployments/dev/systemd/`](deployments/dev/systemd/:1)
- [`.github/workflows/deploy-dev.yml`](.github/workflows/deploy-dev.yml:1)
