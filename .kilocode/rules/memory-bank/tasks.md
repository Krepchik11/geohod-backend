# Tasks: Geohod Backend

This document captures repetitive workflows to execute similar changes quickly and consistently. Follow these steps when performing the associated tasks.

## Add or Update v2 API Endpoint

Last verified: 2025-08-05

Files typically involved:
- Controller: src/main/java/me/geohod/geohodbackend/api/controller/v2/...
- DTOs:
  - Request: src/main/java/me/geohod/geohodbackend/api/dto/request/...
  - Response: src/main/java/me/geohod/geohodbackend/api/dto/response/...
- Mapper: src/main/java/me/geohod/geohodbackend/api/mapper/...
- Service interface: src/main/java/me/geohod/geohodbackend/service/I*.java
- Service implementation: src/main/java/me/geohod/geohodbackend/service/impl/*.java
- Repositories and projections if needed:
  - src/main/java/me/geohod/geohodbackend/data/model/repository/...
  - src/main/java/me/geohod/geohodbackend/data/dto/...
- ApiResponse wrapper: src/main/java/me/geohod/geohodbackend/api/response/ApiResponse.java
- OpenAPI docs auto-generated (check annotations if needed)

Steps:
1) Define request/response DTO records with validation (if applicable).
2) Add or update MapStruct API mapper interface to translate between DTOs and service layer DTOs/entities.
3) Add controller method in v2 controller:
   - Annotate with appropriate @GetMapping/@PostMapping/etc.
   - Accept request DTO and map to service call.
   - Return ResponseEntity<ApiResponse<T>> with ApiResponse.success(data).
4) Extend service interface and implement in service/impl:
   - Encapsulate business logic, authorization checks, and transactions.
   - Use repositories and projections; do not access DB directly.
5) Update repositories or projections if a new query is required.
6) Run tests and verify OpenAPI (enable dev profile).
7) Security: confirm endpoint falls under /api/v2/** (already authenticated by SecurityConfiguration).

Gotchas:
- Keep v2 responses consistently wrapped with ApiResponse<T>.
- Ensure Telegram authentication is required (no extra permitAll on the path).
- Use projection DTOs for read queries to avoid N+1 and over-fetching.

## Example: Event Participation Check Endpoint (Completed December 2025)

This task was completed in commit 169b127 and serves as a good example of adding a simple read-only endpoint.

**Files involved:**
- Controller: `src/main/java/me/geohod/geohodbackend/api/controller/v2/EventParticipationController.java`
- Response DTO: `src/main/java/me/geohod/geohodbackend/api/dto/response/EventParticipationCheckResponse.java`
- Service interface: `src/main/java/me/geohod/geohodbackend/service/IEventParticipationService.java`
- Service implementation: `src/main/java/me/geohod/geohodbackend/service/impl/EventParticipationService.java`

**Implementation details:**
- Added `GET /api/v2/events/{eventId}/participation/check` endpoint
- Used existing repository method `eventParticipantRepository.existsByEventIdAndUserId()`
- Added comprehensive OpenAPI annotations for API documentation
- Followed v2 API pattern with `ApiResponse<EventParticipationCheckResponse>` return type
- Simple boolean response wrapped in DTO for consistency

**Key aspects:**
- No new database changes required (used existing tables)
- Minimal service layer logic (just validation + repository call)
- Proper authentication via TelegramPrincipal
- Comprehensive error handling (404 for non-existent events)
- OpenAPI documentation for client code generation

## Add Method-Level Security to Endpoints

**Last performed:** December 9, 2025

**Files involved:**
- Controller: `src/main/java/me/geohod/geohodbackend/api/controller/v2/EventParticipationController.java`
- Security service: `src/main/java/me/geohod/geohodbackend/security/EventSecurity.java` (new file)

**Steps:**
1. Create a new security service class (e.g., `EventSecurity`) with methods that validate authorization conditions
2. Add `@Service` annotation to make it a Spring bean
3. Implement authorization logic using `SecurityContextHolder` and repository lookups
4. In the controller, replace manual authorization checks with `@PreAuthorize` annotations
5. Use Spring Security Expression Language (SpEL) to call the security service methods
6. Remove redundant imports and service dependencies that are no longer needed
7. Update OpenAPI documentation to reflect the new security requirements

**Example implementation:**
- Created `EventSecurity.isEventAuthor(UUID eventId)` method that checks if the current user is the event author
- Added `@PreAuthorize("@eventSecurity.isEventAuthor(#eventId)")` to endpoints that require event ownership
- Removed manual checks like `if (!event.authorId().equals(loggedUserId))`

**Benefits:**
- Consistent authorization approach across endpoints
- Reduced boilerplate code
- Better separation of concerns
- Improved security through declarative approach
- Easier to maintain and test authorization logic

**Key aspects:**
- Use Spring Security's method-level security annotations
- Keep authorization logic in dedicated security services
- Ensure proper error handling for unauthorized access
- Update OpenAPI documentation with appropriate response codes (403)

## Adding a Notification via Outbox

Last verified: 2025-08-05

Files:
- Service: src/main/java/me/geohod/geohodbackend/service/impl/...
- Outbox entity/repo: src/main/java/me/geohod/geohodbackend/data/model/TelegramOutboxMessage.java
- Outbox repo: src/main/java/me/geohod/geohodbackend/data/model/repository/TelegramOutboxMessageRepository.java
- Processor/Service: src/main/java/me/geohod/geohodbackend/service/impl/GeohodTelegramBotService.java
- Progress tracking: NotificationProcessorProgress + repository

Steps:
1) Where a business event occurs (e.g., event update), create an outbox record.
2) Persist outbox with necessary payload (chat id, message text).
3) Processor picks records and calls GeohodTelegramBotService.sendMessage(chatId, message).
4) On success, mark progress; on failure, throw TelegramNotificationException and update progress for retries.

Gotchas:
- Ensure transactional boundary includes business write + outbox enqueue for reliability.
- Prepare message templates through configuration where appropriate.

## Update CORS or Security Whitelist

Last verified: 2025-08-05

Files:
- SecurityConfiguration: src/main/java/me/geohod/geohodbackend/configuration/SecurityConfiguration.java

Steps:
1) Update corsConfigurationSource() to add/remove allowed origins.
2) Adjust requestMatchers permitAll/authenticated sections as needed.
3) Re-run and verify access and preflight behavior.

Gotchas:
- Keep swagger and actuator endpoints publicly accessible in dev.
- Maintain /api/v2/** as authenticated.

## Add Configuration Property

Last verified: 2025-08-05

Files:
- application.yml
- GeohodProperties: src/main/java/me/geohod/geohodbackend/configuration/properties/GeohodProperties.java
- ApplicationConfiguration or relevant service for usage

Steps:
1) Add property placeholder to application.yml with ${ENV_VAR} indirection.
2) Extend GeohodProperties record with new property path.
3) Inject and consume in relevant components via constructor injection.

Gotchas:
- Do not log full secrets; mask or log only prefixes as done in ApplicationConfiguration.

## Enable Swagger/OpenAPI locally

Last verified: 2025-08-05

Steps:
1) Start local infra: docker compose up -d
2) Run app with dev profile:
   - ./gradlew bootRun --args='--spring.profiles.active=dev'
3) Access:
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - OpenAPI JSON: http://localhost:8080/api-docs

## Liquibase Migrations

Last verified: 2025-08-05

Configuration:
- Spring config: application.yml spring.liquibase.change-log: classpath:db/changelog/db.changelog-master.xml

Steps:
1) Create new changelog under src/main/resources/db/changelog/.
2) Reference from db.changelog-master.xml or include via nested includes.
3) Run app to apply changes automatically.

Gotchas:
- Keep changes idempotent and order-stable.
- Never modify applied changesets; append new ones.