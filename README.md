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

0. Build project:
```bash
./gradlew clean build -x test
```

1. Start project:
```bash
docker compose up -d --build
```

**To enable Swagger UI and OpenAPI docs:**
   - Set the environment variable (for deployment, CI, or Docker):
     ```bash
     SPRING_PROFILES_ACTIVE=dev
     ```
   - Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## API Versioning

The API supports versioning with backward compatibility:
- **v1**: Legacy endpoints (maintained for backward compatibility)
- **v2**: New endpoints with improved response format using `ApiResponse<T>` wrapper

## Binary link structure protocol

Creates binary data from action and UUID, then encodes base64

17-byte encoded sequences that contain:
- **1 byte** for the action code
- **16 bytes** for the UUID representation

```
[Action Code (1 byte)] [UUID MSB (8 bytes)] [UUID LSB (8 bytes)]
```

### Actions

- **OPEN_EVENT (0x01)**: Opens/view event details
- **REGISTER_FOR_EVENT (0x02)**: Register for event
- **REVIEW_EVENT (0x03)**: Submit review for event

## Database Migrations

This project uses Liquibase for database schema management. Migrations are automatically applied when the application starts.

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
-e GEOHOD_CREATED_EVENT_LINK_TEMPLATE=new_event_link_template \
-e SPRING_PROFILES_ACTIVE=dev \
geohod-backend:latest
```