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

The CI/CD pipeline follows **build-once, promote-by-digest**:

- **PR → `main`** — runs tests (`ci.yml`).
- **Push to `main`** — runs tests → builds image, tagged `sha-<commit>` + `dev-latest` → deploys to dev.
- **Push tag `v*`** — retags the existing `sha-<commit>` image as `v1.2.3` + `latest` (no rebuild from source) → requires manual approval → deploys to prod. Same bytes that ran in dev ship to prod.
- **Manual `Release` dispatch** — retags an existing `v*` image as `latest` and redeploys prod. Use for rollback.

All deploy steps live in a single reusable workflow (`_deploy.yml`): SCP Quadlet units, render `postgres.env` + `app.env`, `podman pull`, `systemctl --user restart`, wait for health. Deploys are serialized per environment via concurrency groups.

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

### Caddy

Backend ports are bound to localhost only (`127.0.0.1`). [Caddy](https://caddyserver.com) proxies inbound traffic and handles TLS automatically via Let's Encrypt.

Install Caddy via the [official packages](https://caddyserver.com/docs/install), then configure `/etc/caddy/Caddyfile`:

```caddy
{
    email your@email.com
}

yourdomain.com {
    reverse_proxy localhost:8080
}

dev.yourdomain.com {
    reverse_proxy localhost:8081
}
```

```bash
sudo systemctl enable --now caddy
```

No certificate management needed — Caddy issues and renews certs automatically.

### Rollback

Open **Actions → Release → Run workflow**, enter the tag to roll back to (e.g. `v1.2.2`). The build step is skipped; only the deploy runs (still requires production approval).

---

## Database migrations

Liquibase runs automatically on startup. No manual steps needed.

## API versioning

- `v1` — legacy endpoints, kept for backward compatibility
- `v2` — current endpoints, responses wrapped in `ApiResponse<T>`
