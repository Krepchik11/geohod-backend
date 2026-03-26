# Geohod Backend

## Local development

### Prerequisites

- Java 23
- Docker / Podman
- Gradle 8.1

### Environment variables

Create `.env` in the project root:

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:54321/geohod
SPRING_DATASOURCE_USERNAME=geohod
SPRING_DATASOURCE_PASSWORD=secret
GEOHOD_TELEGRAM_BOT_TOKEN=your_telegram_bot_token
GEOHOD_TELEGRAM_BOT_USERNAME=your_telegram_bot_username
```

### Run

```bash
# Start Postgres
docker compose up -d

# Run the app
./gradlew bootRun
```

To enable Swagger UI, add `--spring.profiles.active=dev`:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

---

## VPS deployment

The CI/CD pipeline builds images and deploys automatically via Podman Quadlet (systemd-native container management):

- Push to `main` → builds `:dev-latest` → deploys to dev
- Push tag `v*` → builds `:v1.2.3` → requires manual approval → deploys to prod

On every deploy, CI copies the Quadlet unit files from `deployments/` to the VPS and reloads systemd. Containers start automatically on VPS reboot — no manual service management needed.

### Required GitHub configuration

**Secrets** (Settings → Secrets and variables → Actions):

| Name | Value |
|------|-------|
| `VPS_SSH_KEY` | Private SSH key for VPS access |

**Variables** (same page, Variables tab):

| Name | Value |
|------|-------|
| `VPS_HOST` | VPS hostname or IP |
| `VPS_USER` | SSH username |

**Environments** (Settings → Environments):

- `development` — no approval required
- `production` — add required reviewers; restrict to tag pattern `v*`

### One-time VPS bootstrap

Two manual steps required before the first deploy:

**1. Enable user lingering** (so systemd user session starts at boot without login):

```bash
sudo loginctl enable-linger <username>
```

**2. Authenticate with GHCR** (PAT with `read:packages` scope):

```bash
podman login ghcr.io -u <github-username> -p <PAT>
```

**3. Create `.env` files:**

```bash
mkdir -p ~/geohod-backend ~/geohod-backend-dev
```

`~/geohod-backend/.env` (prod) and `~/geohod-backend-dev/.env` (dev):

```properties
POSTGRES_USER=geohod
POSTGRES_PASSWORD=secret
POSTGRES_DB=geohod
SPRING_DATASOURCE_URL=jdbc:postgresql://geohod-postgres:5432/geohod
SPRING_DATASOURCE_USERNAME=geohod
SPRING_DATASOURCE_PASSWORD=secret
GEOHOD_TELEGRAM_BOT_TOKEN=your_token
GEOHOD_TELEGRAM_BOT_USERNAME=your_bot_username
GEOHOD_CREATED_EVENT_LINK_TEMPLATE=https://yourdomain.com/event/{id}
JWT_SECRET=your_jwt_secret
```

After that, trigger a deploy (push to `main` for dev, push a tag for prod) — CI handles everything else.

### nginx

Backend ports are bound to localhost only (`127.0.0.1`). Configure nginx to proxy inbound traffic:

```nginx
# prod
server {
    listen 80;
    server_name yourdomain.com;
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

# dev
server {
    listen 80;
    server_name dev.yourdomain.com;
    location / {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### Rollback

Open **Actions → Release → Run workflow**, enter the tag to roll back to (e.g. `v1.2.2`). The build step is skipped; only the deploy runs (still requires production approval).

---

## Database migrations

Liquibase runs automatically on startup. No manual steps needed.

## API versioning

- `v1` — legacy endpoints, kept for backward compatibility
- `v2` — current endpoints, responses wrapped in `ApiResponse<T>`
