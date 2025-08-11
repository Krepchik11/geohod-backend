It is required to design and implement dev deployment ci/cd using github actions on VPS with linux. Use best practices. That is the initial idea and flow how it should work. I'd like you to act as an experienced software architect and critically evaluate my plan against best practices. Please provide constructive criticism and suggest improvements, especially regarding efficiency, scalability, and maintainability.

## Trigger
- Manual

## Runner build & package
- Checkout code
- Use JDK 23; cache Gradle;
- Build: ./gradlew build -x test
- Podman build using [.github/Dockerfile](.github/Dockerfile:1) tags: geohod-backend:dev-<sha>, geohod-backend:dev-latest
- podman save geohod-backend-dev-<sha>.tar.gz (+ latest copy)
- Render runner env: geohod-dev.env
- Create staging archive: geohod-deploy-artifacts-<sha>.tar.gz (image, systemd units, env)

## Transfer
- SCP artifact ~/geohod-backend-dev/ on host
- Runner removes local sensitive artifacts

## Remote staging & sudo wrapper
- Extract artifact /tmp/geohod-deploy-dev
- Run installer script

## Installer script responsibilities
- requires: podman, systemctl
- Actions:
  - copy env /etc/geohod/geohod-dev.env (root:root, 640)
  - podman load -i <tarball>
  - copy units /etc/systemd/system/*.service (root:root, 644)
  - systemctl daemon-reload; enable & restart units

## Image tag convention
- Primary: geohod-backend:dev-<COMMIT_SHA> (first 7 hex chars)
- Latest: geohod-backend:dev-latest

## References
- Dockerfile: [`.github/Dockerfile`](.github/Dockerfile:1)
## Secrets (used)
- GitHub Secrets (required):
  - POSTGRES_DB
  - POSTGRES_USER
  - POSTGRES_PASSWORD
  - TELEGRAM_BOT_TOKEN
  - TELEGRAM_BOT_USERNAME
  - VPS_USER (SSH username used by actions)
  - VPS_SSH_KEY (private SSH key used by actions)
- GitHub Vars included in runner env:
  - CORS_ALLOWED_ORIGINS
  - GEOHOD_CREATED_EVENT_LINK_TEMPLATE
  - GEOHOD_REVIEW_LINK_TEMPLATE
  - VPS_HOST

## Sudo / host requirements:
  - Deploy user must have NOPASSWD sudo for install script

## More questions
Is that best practice to include postgres also here, or is that better to manage postres separately?