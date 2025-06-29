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

2. Run the application (default profile, Swagger disabled):
```bash
./gradlew bootRun
```

3. **To enable Swagger UI and OpenAPI docs:**
   - Run with the `dev` profile:
     ```bash
     ./gradlew bootRun --args='--spring.profiles.active=dev'
     ```
   - Or set the environment variable (for deployment, CI, or Docker):
     ```bash
     SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
     ```
   - Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## API Versioning

The API supports versioning with backward compatibility:
- **v1**: Legacy endpoints (maintained for backward compatibility)
- **v2**: New endpoints with improved response format using `ApiResponse<T>` wrapper

## Database Migrations

This project uses Liquibase for database schema management. Migrations are automatically applied when the application starts.

## API Endpoints

### Event Management
- `GET /api/events` - List events
- `POST /api/events` - Create event
- `GET /api/events/{id}` - Get event details
- `PUT /api/events/{id}` - Update event
- `DELETE /api/events/{id}` - Cancel event
- `POST /api/events/{id}/finish` - Finish event

### Event Participation
- `POST /api/events/{id}/register` - Register for event
- `POST /api/events/{id}/unregister` - Unregister from event
- `GET /api/events/{id}/participants` - Get event participants
- `DELETE /api/events/{id}/participants/{userId}` - Remove participant

### Reviews & Ratings (v2)
- `POST /api/v2/reviews` - Submit review (authenticated user only)
- `GET /api/v2/users/{id}/reviews` - Get user reviews (paged)
- `GET /api/v2/users/{id}/rating` - Get user average rating
- `PATCH /api/v2/reviews/{id}/hide` - Hide review (event organizer only)
- `PATCH /api/v2/reviews/{id}/unhide` - Unhide review (event organizer only)

### Notifications (v2)
- `GET /api/v2/notifications` - Get notifications (cursor-based pagination)
  - Query params: `limit` (default: 20), `isRead` (default: false), `cursorId` (optional)
- `POST /api/v2/notifications/{id}/dismiss` - Mark notification as read
- `POST /api/v2/notifications/dismiss-all` - Mark all notifications as read

### Authentication
All endpoints require Telegram authentication via `Authentication` header.

## Prod deployment

### Required environment variables

- `SPRING_DATASOURCE_URL` - PostgreSQL URL
- `SPRING_DATASOURCE_USERNAME` - DB username
- `SPRING_DATASOURCE_PASSWORD` - DB password
- `GEOHOD_TELEGRAM_BOT_TOKEN` - Telegram bot API token
- `GEOHOD_TELEGRAM_BOT_USERNAME` - Telegram bot username
- `GEOHOD_CREATED_EVENT_LINK_TEMPLATE` - Link template for event created notification
  - Parameters: `{botName}`, `{eventId}`

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
- `SECRET_VARS` - Comma-separated list of environment variables in format `KEY=VALUE`