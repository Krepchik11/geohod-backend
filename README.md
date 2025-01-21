# Geohod Backend

## Prerequisites

- Java 23
- Docker and Docker Compose
- Gradle 8.1

## Dev run

### Environment vars

Create an `.env` file in the root directory with the following variables:
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:54321/geohod
SPRING_DATASOURCE_USERNAME=geohod
SPRING_DATASOURCE_PASSWORD=secret
GEOHOD_TELEGRAM_BOT_TOKEN=your_telegram_bot_token
GEOHOD_TELEGRAM_BOT_USERNAME=your_telegram_bot_username
```

### Run locally

1. Start the required services:
```bash
docker compose up -d
```

2. Run the application:
```bash
./gradlew bootRun
```

Application available at http://localhost:8080

## Prod deployment

### Required environment variables

- `SPRING_DATASOURCE_URL` - PostgreSQL URL
- `SPRING_DATASOURCE_USERNAME` - DB username
- `SPRING_DATASOURCE_PASSWORD` - DB password
- `GEOHOD_TELEGRAM_BOT_TOKEN` - Telegram bot API token
- `GEOHOD_TELEGRAM_BOT_USERNAME` - Telegram bot username

### Docker deployment

1. Build image:
```bash
docker build -t geohod-backend:latest .
```

2. Run container:
```bash
docker run -d \
--name geohod \
-p 8080:8080 \
-e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/geohod \
-e SPRING_DATASOURCE_USERNAME=your_username \
-e SPRING_DATASOURCE_PASSWORD=your_password \
-e GEOHOD_TELEGRAM_BOT_TOKEN=your_token \
-e GEOHOD_TELEGRAM_BOT_USERNAME=your_bot_username \
geohod-backend:latest
```

### GitHub Actions deployment

Project includes GitHub Actions workflow that automatically builds and deploys the application new release published.

1. Builds the application
2. Creates a Docker image
3. Transfers the image to the production VPS
4. Runs container with the specified environment variables

Required GitHub Variables:
- `VPS_HOST` - VPS hostname or IP address

Required GitHub Secrets for deployment:
- `VPS_USER` - VPS username
- `VPS_SSH_KEY` - SSH private key for VPS access
- `VPS_SSH_FINGERPRINT` - VPS SSH fingerprint
- `SECRET_VARS` - Comma-separated list of environment variables in format `KEY=VALUE`