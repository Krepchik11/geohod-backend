# Active Context: Geohod Backend

## Current Work Focus

The Geohod backend is in active development focusing on solidifying the v2 API, Telegram-based authentication, and reliable notification delivery via an outbox pattern. The codebase uses Spring Data JDBC, MapStruct, and Liquibase with a clear layered architecture.

## Recent Changes (derived from repository)

### Latest Updates (December 2025)

1. **User Settings Enhancement**
   - Added `phoneNumber` field to `UserSettings` entity with dedicated CRUD operations
   - New endpoint: `PUT /api/v2/user-settings/phone-number` via `PhoneNumberRequest` DTO
   - Database migration: `db.changelog-2.5-add-phone-number.xml`
   - Phone number exposed in `UserSettingsResponse` and `UserSettingsDto`

2. **Notification System Refactor** (Major architectural change)
   - **Channel-specific strategies**: Separated notification strategies by channel (In-App vs Telegram)
   - **New architecture**:
     - `NotificationChannel` enum: IN_APP, TELEGRAM
     - `NotificationConfiguration` class for strategy configuration
     - Refactored `NotificationStrategy` interface to be channel-agnostic
   - **Strategy reorganization**:
     - Deleted unified strategies: `EventCancelledStrategy`, `EventCreatedStrategy`, `EventFinishedStrategy`, `ParticipantRegisteredStrategy`, `ParticipantUnregisteredStrategy`
     - Created channel-specific strategies:
       - In-App: `EventCancelledInAppStrategy`, `EventCreatedInAppStrategy`, `EventFinishedInAppStrategy`, `ParticipantRegisteredInAppStrategy`, `ParticipantUnregisteredInAppStrategy`
       - Telegram: `EventCancelledOrganizerNoNotifyTelegramStrategy`, `EventCancelledOrganizerNotifyParticipantsTelegramStrategy`, `EventCreatedTelegramStrategy`, `EventFinishedTelegramStrategy`, `ParticipantRegisteredTelegramStrategy`, `ParticipantUnregisteredTelegramStrategy`
   - **Processor updates**:
     - `InAppNotificationProcessor` and `TelegramNotificationProcessor` refactored to work with channel-specific strategies
     - `StrategyRegistry` updated to support channel-based strategy resolution
   - **Template improvements**:
     - `MessageTemplateRegistry` simplified and reorganized
     - `MessageFormatter` refactored for better maintainability
   - Added `startAppLink` template to `GeohodProperties`

3. **Event Management Improvements**
   - Enhanced OpenAPI descriptions for event endpoints
   - Improved `CreateEventDto` mapping using constructor pattern
   - EventParticipantDetails and projections updated to support phone number

4. **Event Participation Check Endpoint** (December 2025)
   - Added `GET /api/v2/events/{eventId}/participation/check` endpoint
   - New DTO: `EventParticipationCheckResponse` with OpenAPI schema annotations
   - Service method: `IEventParticipationService.isUserParticipant(UUID userId, UUID eventId)`
   - Implementation validates event existence and checks participation using existing repository method
   - Comprehensive OpenAPI documentation for code generation compatibility

### Historical Changes

1. API and Docs
   - OpenAPI configuration present with security scheme for Telegram header and a local server entry
   - v2 controllers exist for events, participation, notifications, reviews, users, and user settings
   - ApiResponse<T> wrapper implemented for consistent responses

2. Security
   - Custom Telegram auth implemented:
     - Filter: extracts Authorization header and delegates to AuthenticationManager
     - Provider: verifies Telegram WebApp data, upserts user, sets TelegramPrincipal
     - Stateless session; CORS configured for specific origins
   - Logging filter present in the security chain

3. Data Access
   - Spring Data JDBC used throughout with repositories and projection interfaces
   - Liquibase configured via application.yml (classpath:db/changelog/db.changelog-master.xml)

4. Operations & Dev Experience
   - Docker Compose for local Postgres and app
   - Healthcheck via Actuator; dev profile enables Swagger
   - Configuration properties via @ConfigurationProperties (GeohodProperties), including link templates

## Next Steps (immediate)

1. Complete v2 API migration and ensure consistent ApiResponse<T> usage across endpoints.
2. Harden notification outbox processing with retries, metrics, and error tracking.
3. Expand integration tests with Testcontainers for critical flows (auth, events, participation, reviews).
4. Performance passes on slow queries; add indexes where needed.
5. Ensure OpenAPI is accurate for v2 and dev profile toggles are documented.

## Environment & Configuration Snapshot

- Java 23, Spring Boot 3.3.5, Gradle 8.x.
- Liquibase enabled; change-log at db/changelog/db.changelog-master.xml.
- Actuator health exposed; Swagger/OpenAPI disabled by default, enabled in dev.
- Geohod properties:
  - geohod.telegram-bot.token / username
  - geohod.linkTemplates.eventRegistrationLink / reviewLink
  - geohod.processor.in-app.delay / telegram.delay

## Known Constraints

- Telegram is the sole authentication mechanism.
- Stateless security; sessions disabled.
- CORS limited to configured origins.
- DB schema management strictly via Liquibase.

## Risks & Watch Items

- Telegram API rate limits and delivery errors (ensure retries/backoff).
- Consistency between v1 (legacy) and v2 responses.
- Liquibase changelog completeness and idempotency across environments.
- Proper masking of secrets in logs (bot token partially logged on startup).

## References (source anchors)

- Build and dependencies: build.gradle
- App config: application.yml
- Security chain: SecurityConfiguration
- Telegram filter/provider/tokens: security.filter.TelegramInitDataAuthenticationFilter, security.provider.TelegramTokenAuthenticationProvider, security.token.TelegramTokenAuthentication
- OpenAPI: configuration.OpenApiConfiguration
- Properties: configuration.properties.GeohodProperties
- Telegram bot service: service.impl.GeohodTelegramBotService