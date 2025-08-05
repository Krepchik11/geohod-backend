# Architecture: Geohod Backend

## System Overview

Geohod Backend is a layered Spring Boot application organized into API, Service, Data Access, and Infrastructure layers. It exposes versioned REST APIs (v1 legacy, v2 primary), authenticates users via Telegram WebApp init data, and delivers notifications reliably using an outbox-style approach. The system is stateless with Liquibase-managed schema and Docker-based local environment.

## Source Code Layout

- Application entry
  - src/main/java/me/geohod/geohodbackend/GeohodBackendApplication.java
- API Layer
  - Controllers
    - .../api/controller/ (v1 legacy controllers)
    - .../api/controller/v2/ (v2 controllers: EventController, EventParticipationController, NotificationController, ReviewController, UserController)
  - DTOs
    - .../api/dto/ (TelegramInitDataDto, review and notification DTOs, request/response DTOs)
  - Mappers
    - .../api/mapper/ (EventApiMapper, ReviewApiMapper, NotificationApiMapper, UserApiMapper)
  - Response wrapper
    - .../api/response/ApiResponse.java
- Service Layer
  - Interfaces: .../service/I*.java
  - Implementations: .../service/impl/*.java (EventService, EventManager, Participation services, ReviewServiceImpl, GeohodTelegramBotService, etc.)
  - Processors and notification services: .../service/notification, .../service/processor (present for extensibility)
- Data Access Layer
  - Models/Entities: .../data/model/** (Event, EventParticipant, User, Review, TelegramOutboxMessage, Notification, NotificationProcessorProgress, EventLog)
  - Repositories: .../data/model/repository/** (Spring Data JDBC repositories and projections)
  - DTOs/Projections: .../data/dto/** (projections and internal DTOs, e.g., EventDetailedProjection, ReviewWithAuthorDto, UserRatingDto)
  - Mappers: .../data/mapper/** (MapStruct mappers for entities & internal DTOs)
- Configuration & Infrastructure
  - Security: .../configuration/SecurityConfiguration.java
  - OpenAPI: .../configuration/OpenApiConfiguration.java
  - Application setup: .../configuration/ApplicationConfiguration.java
  - Properties: .../configuration/properties/GeohodProperties.java
  - LoggingFilter, JdbcConfiguration (if needed)
  - application.yml, Liquibase changelog at classpath:db/changelog/db.changelog-master.xml
- Exceptions
  - .../exception/** (GeohodException, TelegramNotificationException)
- Tests
  - src/test/java/me/geohod/geohodbackend/** (services, notifications, user_settings)

## Layered Architecture

1) API Layer
   - Controllers validate and map request DTOs to service calls.
   - Responses are wrapped using ApiResponse<T> in v2.
   - OpenAPI configured with a Telegram API Key security scheme and dev-only server entry.
2) Service Layer
   - Encapsulates business logic, authorization checks, and orchestration across repositories.
   - Employs transactional boundaries for multi-repository operations.
   - Notification and bot services abstract telegrambots integration and outbox processing.
3) Data Access Layer
   - Spring Data JDBC repositories for CRUD and custom queries.
   - Projection interfaces and internal DTOs to optimize reads.
   - Entities model core tables (events, users, participants, reviews, outbox, logs).
4) Infrastructure Layer
   - Security with custom Telegram filter and provider.
   - Configuration properties via @ConfigurationProperties for Telegram bot and link templates.
   - Liquibase for schema migrations; Actuator for health.

## Key Technical Decisions

- API Versioning
  - v1 maintained for backward compatibility.
  - v2 is the primary surface with consistent ApiResponse<T> wrapper.
- Authentication
  - Custom Telegram WebApp init data authentication:
    - Filter: TelegramInitDataAuthenticationFilter extracts Authorization header
    - Provider: TelegramTokenAuthenticationProvider validates token, upserts user, sets TelegramPrincipal via TelegramTokenAuthentication
  - Stateless sessions with CORS configured for specific origins.
- Data Access
  - Spring Data JDBC chosen over JPA for simplicity and predictable SQL.
  - Use of projections (DTO interfaces) for read performance.
- Notifications
  - Outbox-style pattern for reliable delivery:
    - TelegramOutboxMessage persisted as part of business flows
    - Asynchronous processing via service components
    - GeohodTelegramBotService wraps telegrambots API and throws domain exceptions on failures
- Documentation & Ops
  - OpenAPI available in dev profile; production disables Swagger UI.
  - Actuator health endpoints exposed; Docker compose used for local infra.

## Security Flow

1) Request received with Authorization header containing Telegram init data token.
2) TelegramInitDataAuthenticationFilter creates TelegramTokenAuthentication and delegates to AuthenticationManager.
3) TelegramTokenAuthenticationProvider verifies token (TelegramTokenService), extracts Telegram user data, createOrUpdateUser, and returns authenticated TelegramTokenAuthentication with TelegramPrincipal.
4) SecurityContext populated; request continues to controller.
5) Session policy STATELESS; CORS configured for allowed origins and methods.

Files:
- SecurityConfiguration.java
- TelegramInitDataAuthenticationFilter.java
- TelegramTokenAuthenticationProvider.java
- TelegramTokenAuthentication.java
- configuration/properties/GeohodProperties.java

## API Design

- Versioned endpoints:
  - /api/v2/**: authenticated endpoints for events, participation, notifications, reviews, users
  - Public endpoints: actuator and swagger docs (dev)
- Response format:
  - ApiResponse.success(data), ApiResponse.error(message)
- DTOs:
  - Dedicated request/response records per entity and operation
- OpenAPI:
  - Security scheme: API key in header named "Authorization"
  - Local server entry for development

## Database & Migrations

- PostgreSQL as the primary datastore.
- Liquibase enabled; master changelog at classpath:db/changelog/db.changelog-master.xml.
- Entities: Event, EventParticipant, User, Review, Notification, TelegramOutboxMessage, NotificationProcessorProgress, EventLog, UserRating.
- Indexing and constraints implemented via changelogs (review uniqueness per event/user implied from service logic and repository projections).

## Notification Processing

- Outbox enqueue on business events (e.g., event changes, participation).
- Processor components poll outbox and dispatch via:
  - GeohodTelegramBotService → Telegram Bot API (telegrambots starter).
- Retry and error handling:
  - Domain exception TelegramNotificationException thrown on Telegram API failure.
  - NotificationProcessorProgress tracks processing state.

## Configuration Management

- application.yml
  - spring.liquibase.change-log: classpath:db/changelog/db.changelog-master.xml
  - springdoc swagger and api-docs disabled by default
  - management endpoints (health, info) exposure
  - geohod.telegram-bot token/username
  - geohod.linkTemplates.eventRegistrationLink, reviewLink
  - geohod.processor.in-app.delay, telegram.delay
- GeohodProperties provides typed access to above values.
- ApplicationConfiguration logs bot token prefix at startup for verification.

## Deployment & Local Environment

- Docker Compose
  - postgres (17-alpine) at 5432, healthchecked
  - app image built from .github/Dockerfile
  - app exposed on host 8081→container 8080
  - Env driven configuration via compose variables
- README documents dev profile enabling Swagger and health endpoints.

## Design Patterns in Use

- Layered Architecture
- Repository Pattern (Spring Data JDBC)
- DTO Pattern (API and internal projections)
- Outbox Pattern for notifications
- Strategy-ready notification processors (in-app, telegram)
- Global Exception Handling via GlobalExceptionHandler
- Factory/Builder applied implicitly via MapStruct mappers

## Critical Flows

Authentication:
Client → SecurityFilterChain → TelegramInitDataAuthenticationFilter → AuthenticationManager → TelegramTokenAuthenticationProvider → TelegramTokenAuthentication(TelegramPrincipal) → Controller

Event & Participation:
Controller(v2) → Service(impl) → Repositories (Event, EventParticipant, Projections) → Entities → Liquibase-managed DB
Side-effect: Outbox created → Processor → GeohodTelegramBotService → Telegram

Reviews & Ratings:
ReviewController(v2) → ReviewServiceImpl → ReviewRepository → UserRatingRepository/Projection → Aggregate rating calculation

## Observability

- Actuator health endpoint for container health checks (compose uses wget against /actuator/health).
- LoggingFilter in security chain for request logging.
- Future: metrics and additional logging around outbox processing.