# Geohod Backend

## Local development

### Prerequisites

- Java 23
- Docker or Podman + Compose plugin
- Gradle 8.1

### 1. Create env files

```bash
cp postgres.env.template postgres.env
cp app.env.template app.env
```

Edit `postgres.env` — set your local database credentials:

```properties
POSTGRES_USER=geohod
POSTGRES_PASSWORD=secret
POSTGRES_DB=geohod
```

Edit `app.env` — at minimum set these three (leave the rest as-is for local dev):

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/geohod   # keep "postgres" — it's the compose service name
SPRING_DATASOURCE_USERNAME=geohod                               # must match POSTGRES_USER
SPRING_DATASOURCE_PASSWORD=secret                               # must match POSTGRES_PASSWORD

GEOHOD_TELEGRAM_BOT_TOKEN=your-bot-token
GEOHOD_TELEGRAM_BOT_USERNAME=your-bot-username
GEOHOD_SECURITY_JWT_SECRET=any-random-string-at-least-32-chars
```

All other vars in `app.env` are optional for local development (mail is disabled by default).

### 2. Run with Docker / Podman Compose

```bash
docker compose up --build       # or: podman compose up --build
```

The app starts after Postgres passes its health check.

- API: http://localhost:8080
- Health: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html *(dev profile enabled automatically in compose)*

### Run without Compose (IDE / Gradle)

Start Postgres first:

```bash
docker compose up postgres -d
```

Then run the app with the dev profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Env vars must be set in your shell or IDE run configuration. The ones required are the same as `app.env` above — `SPRING_DATASOURCE_*`, `GEOHOD_TELEGRAM_BOT_TOKEN`, `GEOHOD_TELEGRAM_BOT_USERNAME`, `GEOHOD_SECURITY_JWT_SECRET`.

---

## VPS deployment

The CI/CD pipeline builds images and deploys automatically via Podman Quadlet (systemd-native container management):

- Push to `main` → builds `:dev-latest` → deploys to dev
- Push tag `v*` → builds `:v1.2.3` → requires manual approval → deploys to prod

On every deploy, CI copies the Quadlet unit files from `deployments/` to the VPS and reloads systemd. Containers start automatically on VPS reboot — no manual service management needed.

### Required GitHub configuration

See `deployments/dev/setup.md` and `deployments/prod/setup.md` for the full secrets/variables tables.

**Environments** (Settings → Environments):

- `development` — no approval required
- `production` — add required reviewers; restrict to tag pattern `v*`

### One-time VPS bootstrap

**1. Enable user lingering** (so systemd user session starts at boot without login):

```bash
sudo loginctl enable-linger <username>
```

**2. Authenticate with GHCR** (PAT with `read:packages` scope):

```bash
podman login ghcr.io -u <github-username> -p <PAT>
```

After that, trigger a deploy (push to `main` for dev, push a tag for prod). CI writes `postgres.env` and `app.env` on the VPS automatically — no manual file creation needed.

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
