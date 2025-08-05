# Tech: Geohod Backend

## Technologies Used
- Language: Java 23 (Gradle toolchain)
- Framework: Spring Boot 3.3.5
- Build: Gradle 8.x (wrapper committed)
- Persistence: Spring Data JDBC
- Database: PostgreSQL
- Migrations: Liquibase (classpath:db/changelog/db.changelog-master.xml)
- Security: Spring Security (custom Telegram auth)
- API Docs: springdoc-openapi-starter-webmvc-ui (Swagger UI enabled in dev profile)
- Bot Integration: telegrambots-spring-boot-starter 6.9.7.1
- Mapping: MapStruct 1.5.5.Final
- Utilities: Lombok
- Monitoring: Spring Boot Actuator

## Configuration & Profiles
- application.yml
  - server.port: 8080
  - spring.datasource: URL/username/password via env
  - liquibase.enabled: true, change-log path configured
  - springdoc:
    - api-docs.enabled: false (default)
    - swagger-ui.enabled: false (default), path /swagger-ui.html
  - management.endpoints.web.exposure.include: health,info
  - geohod:
    - telegram-bot.token / username from env (GEOHOD_TELEGRAM_BOT_TOKEN / GEOHOD_TELEGRAM_BOT_USERNAME)
    - linkTemplates.eventRegistrationLink / reviewLink from env
    - processor delays for in-app and telegram
- Dev profile (activated with SPRING_PROFILES_ACTIVE=dev) enables Swagger/OpenAPI as per README guidance

## Environment Variables (README)
- SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:54321/geohod
- SPRING_DATASOURCE_USERNAME=geohod
- SPRING_DATASOURCE_PASSWORD=secret
- GEOHOD_TELEGRAM_BOT_TOKEN=your_telegram_bot_token
- GEOHOD_TELEGRAM_BOT_USERNAME=your_telegram_bot_username
- For compose:
  - TELEGRAM_BOT_TOKEN / TELEGRAM_BOT_USERNAME
  - CORS_ALLOWED_ORIGINS
  - SPRING_PROFILES_ACTIVE (defaults to dev for container)

## Security Design
- Stateless Spring Security with CORS and CSRF disabled
- Public endpoints:
  - /actuator/**
  - /swagger-ui/**, /swagger-ui.html, /v3/api-docs/**, /v3/api-docs.yaml
- Authenticated endpoints:
  - /api/v2/**
  - All others fullyAuthenticated()
- Custom Telegram flow:
  - TelegramInitDataAuthenticationFilter reads Authorization header
  - TelegramTokenAuthenticationProvider validates token (TelegramTokenService), upserts user (UserService), sets TelegramPrincipal via TelegramTokenAuthentication
- CORS Allowed origins:
  - https://client.geohod.ru
  - https://localhost:3000
  - https://127.0.0.1:3000

## Project Structure (selected)
- API Layer:
  - Controllers: v1 and v2 packages (v2: Event, EventParticipation, Notification, Review, User)
  - DTOs: request/response objects, TelegramInitDataDto, review/notification DTOs
  - Mappers: MapStruct mappers for API↔internal DTOs
  - ApiResponse<T> wrapper with static success/error
- Service Layer:
  - Interfaces: IEventService, IEventManager, IEventParticipationService, IEventProjectionService, IAppNotificationService, etc.
  - Implementations: EventService, EventManager, EventParticipationService, ReviewServiceImpl, GeohodTelegramBotService, etc.
- Data Access Layer:
  - Entities: Event, EventParticipant, User, Review, TelegramOutboxMessage, Notification, NotificationProcessorProgress, EventLog, UserRating
  - Repositories: Spring Data JDBC repositories, projections (EventProjectionRepository, UserRatingRepository, etc.)
  - Mappers: EventModelMapper, UserModelMapper, NotificationMapper
  - DTOs/Projections: EventDetailedProjection, ReviewWithAuthorDto, UserRatingDto, etc.
- Configuration:
  - SecurityConfiguration
  - OpenApiConfiguration
  - ApplicationConfiguration (+ @ConfigurationProperties GeohodProperties)
  - LoggingFilter, JdbcConfiguration
- Resources:
  - Liquibase change-log: classpath:db/changelog/db.changelog-master.xml

## Local Development
- Start infra: docker compose up -d (compose.yml includes postgres and app service)
  - Postgres 17-alpine @ 5432 with persistent volume
  - App built from .github/Dockerfile, exposed 8081:8080
  - Healthcheck via /actuator/health
- Run app:
  - ./gradlew bootRun
  - Enable dev profile for Swagger:
    - ./gradlew bootRun --args='--spring.profiles.active=dev'
    - or SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
- Swagger (dev):
  - UI: http://localhost:8080/swagger-ui.html
  - OpenAPI JSON: http://localhost:8080/api-docs

## Testing
- JUnit Platform, Spring Boot Test
- spring-security-test
- Testcontainers (junit-jupiter and postgresql) for DB integration tests

## Deployment
- Docker-based, with compose for local and podman-based manifests under deployments/
- Prod systemd files and scripts present under deployments/prod
- Environment-driven configuration
- Actuator health endpoints used by container healthcheck

## Technical Constraints & Considerations
- Telegram is the only authentication provider
- Stateless sessions, CORS limited to specific origins
- Liquibase governs all schema changes
- Performance focus on JDBC with projections and DTOs
- Reliable notification delivery via outbox pattern and Telegram bot wrapper

## Notable Classes (anchors)
- SecurityConfiguration.java
- TelegramInitDataAuthenticationFilter.java
- TelegramTokenAuthenticationProvider.java
- TelegramTokenAuthentication.java
- configuration/properties/GeohodProperties.java
- configuration/OpenApiConfiguration.java
- api/response/ApiResponse.java
- service/impl/GeohodTelegramBotService.java