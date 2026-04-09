# Dev Deployment Setup

## One-Time Server Setup

```bash
sudo useradd -m -s /bin/bash geohod
sudo loginctl enable-linger geohod
# Add VPS_SSH_KEY public key to ~/.ssh/authorized_keys for the geohod user
```

## GitHub Environment: `development`

Go to **Settings → Environments → development** and configure:

### Secrets

| Secret | Description |
|--------|-------------|
| `VPS_SSH_KEY` | Private SSH key for the deploy user |
| `DB_PASSWORD` | Database password |
| `SPRING_MAIL_PASSWORD` | SMTP account password |
| `GEOHOD_TELEGRAM_BOT_TOKEN` | Telegram bot token |
| `GEOHOD_SECURITY_TELEGRAM_OIDC_CLIENT_ID` | Telegram OIDC client ID |
| `GEOHOD_SECURITY_TELEGRAM_OIDC_CLIENT_SECRET` | Telegram OIDC client secret |
| `GEOHOD_SECURITY_JWT_SECRET` | JWT signing secret (≥32 chars) |

### Variables

| Variable | Example |
|----------|---------|
| `VPS_HOST` | `dev.geohod.ru` |
| `VPS_USER` | `geohod` |
| `DB_USER` | `geohod` |
| `DB_NAME` | `geohod` |
| `SPRING_MAIL_HOST` | `smtp.example.com` |
| `SPRING_MAIL_PORT` | `587` |
| `SPRING_MAIL_USERNAME` | `noreply@geohod.ru` |
| `GEOHOD_TELEGRAM_BOT_USERNAME` | `GeohodDevBot` |
| `GEOHOD_CORS_ALLOWED_ORIGINS` | `https://app.dev.geohod.ru,http://localhost:3000` |

## How It Works

Each deploy writes two files on the VPS (regenerated every run):

- `~/geohod-backend-dev/postgres.env` → loaded by the postgres container
- `~/geohod-backend-dev/app.env` → loaded by the Spring backend container

Both files get `chmod 600` (owner-readable only).

## Local Dev with Docker/Podman Compose

```bash
# 1. Copy templates
cp postgres.env.template postgres.env
cp app.env.template app.env

# 2. Edit app.env — set at minimum:
#    GEOHOD_TELEGRAM_BOT_TOKEN, GEOHOD_TELEGRAM_BOT_USERNAME, GEOHOD_SECURITY_JWT_SECRET
#    Leave SPRING_DATASOURCE_URL as-is (points to the compose postgres service)

# 3. Run
docker compose up --build        # or: podman compose up --build

# 4. Access
#    API:    http://localhost:8080
#    Health: http://localhost:8080/actuator/health
```
