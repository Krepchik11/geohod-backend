# Deploy

Quick steps to deploy Geohod backend dev.

Preconditions
- Podman installed on host.
- Admin added narrow sudoers entry(s) for the deploy user.
- CI has these GitHub Secrets: POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD, TELEGRAM_BOT_TOKEN, TELEGRAM_BOT_USERNAME.

One-time admin (install wrappers)
- Copy and secure the dev wrapper:
  sudo cp ~/geohod-backend/deployments/dev/geohod-deploy-systemd-dev.sh /usr/local/bin/geohod-deploy-systemd-dev.sh
  sudo chown root:root /usr/local/bin/geohod-deploy-systemd-dev.sh && sudo chmod 750 /usr/local/bin/geohod-deploy-systemd-dev.sh

- Add visudo entries for the deploy user (example):
  deployuser ALL=(root) NOPASSWD: /usr/local/bin/geohod-deploy-systemd-dev.sh

CI deploy flow (current; dev)
- CI asserts required secrets early and checks sudoers on host (expects `/usr/local/bin/geohod-deploy-systemd-dev.sh`).
- CI builds the image and tags it with:
  - PRIMARY_TAG=geohod-backend:dev-${COMMIT_SHA}
  - LATEST_TAG=geohod-backend:dev-latest
- CI saves the image(s) into a versioned image tar: `geohod-backend-dev-${COMMIT_SHA}.tar.gz` and also produces a `geohod-backend-dev-latest.tar.gz` copy.
- CI packages a single deployment archive (runner-side) named:
  - `geohod-deploy-artifacts-${COMMIT_SHA}.tar.gz`
  This archive contains:
  - the image tar(s) (versioned + latest),
  - `deployments/dev/systemd/*.service`,
  - `geohod-dev.env` (rendered from secrets),
  - `deployments/shared/healthcheck.sh`.
  Packaging on the runner preserves strict file permissions on secrets while avoiding permission errors during SCP.
- CI SCPs the single archive to the host and removes runner artifacts.
- CI SSHs to the host, extracts the archive into `/tmp/geohod-deploy-staging`, and invokes the privileged wrapper:
  sudo /usr/local/bin/geohod-deploy-systemd-dev.sh install /tmp/geohod-deploy-staging
  (The wrapper is responsible for loading the image, placing secrets into `/etc/geohod`, installing systemd units, and starting services.)

Quick verify (dev)
- systemctl is-active container-geohod-backend-dev.service
- curl --fail http://localhost:8081/actuator/health
- sudo ls -l /etc/geohod

Key files
- [`deployments/dev/geohod-deploy-systemd-dev.sh`](deployments/dev/geohod-deploy-systemd-dev.sh:1)
- [`deployments/dev/systemd/`](deployments/dev/systemd/:1)
- [`deployments/shared/healthcheck.sh`](deployments/shared/healthcheck.sh:1)
- [`.github/workflows/deploy-dev.yml`](.github/workflows/deploy-dev.yml:1)
