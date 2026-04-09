# Prod Deployment Setup

## One-Time Server Setup

```bash
sudo useradd -m -s /bin/bash geohod
sudo loginctl enable-linger geohod
# Add VPS_SSH_KEY public key to ~/.ssh/authorized_keys for the geohod user
```

## GitHub Environment: `production`

Go to **Settings → Environments → production** and configure:

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
| `VPS_HOST` | `geohod.ru` |
| `VPS_USER` | `geohod` |
| `DB_USER` | `geohod` |
| `DB_NAME` | `geohod` |
| `SPRING_MAIL_HOST` | `smtp.example.com` |
| `SPRING_MAIL_PORT` | `587` |
| `SPRING_MAIL_USERNAME` | `noreply@geohod.ru` |
| `GEOHOD_TELEGRAM_BOT_USERNAME` | `GeohodBot` |
| `GEOHOD_CORS_ALLOWED_ORIGINS` | `https://app.geohod.ru` |

## How It Works

Each deploy writes two files on the VPS (regenerated every run):

- `~/geohod-backend/postgres.env` → loaded by the postgres container
- `~/geohod-backend/app.env` → loaded by the Spring backend container

Both files get `chmod 600` (owner-readable only).
