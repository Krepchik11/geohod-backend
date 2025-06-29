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

## Database Migrations

This project uses Liquibase for database schema management. Migrations are automatically applied when the application starts.

### Migration Files

- `db.changelog-1.0.xml` - Initial schema
- `db.changelog-1.1-reviews.xml` - Reviews table
- `db.changelog-1.2-user_ratings.xml` - User ratings table
- `db.changelog-1.3-event_logs.xml` - Event logs table
- `db.changelog-1.4-npp.xml` - Notification processor progress table
- `db.changelog-1.5-notifications.xml` - Notifications table
- `db.changelog-1.6-notification-id-numeric.xml` - Changed notification ID to numeric

### Running Migrations Manually

To run migrations manually:
```bash
./gradlew liquibaseUpdate
```

To check migration status:
```bash
./gradlew liquibaseStatus
```

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
All endpoints require Telegram authentication via `X-Telegram-Init-Data` header.

## Features

### User Reputation and Feedback
- **User Rating System**: Calculates average rating for event organizers based on all reviews
- **Event Review System**: Allows participants to leave reviews and ratings for attended events
- **Review Moderation**: Event organizers can hide/unhide reviews without affecting rating calculations

### In-App Notifications
- **Cursor-based Pagination**: Efficient long polling with numeric ID-based cursors
- **Notification Management**: Mark individual or all notifications as read
- **Automated Generation**: Notifications are generated from event logs for:
  - Event registration
  - Event cancellation
  - Review submissions

### Event Logging
- **Decoupled Architecture**: Events are logged first, then processed asynchronously
- **Scheduled Processors**: Background processors consume event logs to generate notifications
- **Telegram Integration**: Seamless integration with existing Telegram notification system

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