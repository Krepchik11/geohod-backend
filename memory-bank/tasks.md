# Tasks: Geohod Backend

This document captures repetitive workflows to execute similar changes quickly and consistently. Follow these steps when performing the associated tasks.

## Event Sorting Implementation

**Last verified**: 2025-11-23 - **COMPLETED**

Files involved:
- Repository: `src/main/java/me/geohod/geohodbackend/data/model/repository/EventProjectionRepository.java`
- Test: `src/test/java/me/geohod/geohodbackend/data/model/repository/EventProjectionRepositoryTest.java`

**Implementation Details**:
1. **Repository Enhancement**: Modified the `events()` method to dynamically build ORDER BY clause based on `Pageable` sort parameters
2. **Sorting Logic**: Implemented `buildOrderByClause()` private method with switch statement mapping:
   - `name` → `e.name`
   - `date` → `e.date` 
   - `status` → `e.status`
   - `createdAt` → `e.created_at`
   - `updatedAt` → `e.updated_at`
   - Default fallback to `e.created_at DESC`
3. **Test Coverage**: Created comprehensive integration tests using Testcontainers with real PostgreSQL:
   - `shouldSortEventsByNameAsc()` - Ascending name sorting
   - `shouldSortEventsByDateDesc()` - Descending date sorting
   - `shouldSortEventsByStatus()` - Status-based sorting
   - `shouldSortEventsByCreatedAtDefault()` - Default created_at DESC behavior
4. **OpenAPI Documentation**: Added comprehensive sorting documentation to EventController with:
   - Detailed field descriptions and examples
   - Sorting format specifications (?sort=field,direction)
   - Available sort fields and directions
   - Practical examples for frontend developers
   - Parameter documentation for Swagger UI
5. **Backward Compatibility**: Works with and without sort parameters

**Verification Results**: All 4 tests passed successfully with Testcontainers integration

## Add or Update v2 API Endpoint

Last verified: 2025-11-23

Files typically involved:
- Controller: src/main/java/me/geohod/geohodbackend/api/controller/v2/...
- DTOs:
  - Request: src/main/java/me/geohod/geohodbackend/api/dto/request/... (record-based)
  - Response: src/main/java/me/geohod/geohodbackend/api/dto/response/... (record-based)
- Mapper: src/main/java/me/geohod/geohodbackend/api/mapper/...
- Service interface: src/main/java/me/geohod/geohodbackend/service/I*.java
- Service implementation: src/main/java/me/geohod/geohodbackend/service/impl/*.java
- Repositories and projections if needed:
  - src/main/java/me/geohod/geohodbackend/data/model/repository/...
  - src/main/java/me/geohod/geohodbackend/data/dto/...
- ApiResponse wrapper: src/main/java/me/geohod/geohodbackend/api/response/ApiResponse.java
- OpenAPI docs auto-generated (check annotations if needed)

Steps:
1. Define request/response DTO records with validation annotations (e.g., `@NotNull`, `@Min`, `@Max`).
2. For optional request bodies, design DTOs with default values in service logic to ensure backward compatibility.
3. Add or update MapStruct API mapper interface to translate between API DTOs and service layer DTOs/entities.
4. Add controller method in v2 controller:
   - Annotate with appropriate @GetMapping/@PostMapping/etc.
   - Accept optional request DTO (use `@RequestBody(required = false)` if needed) and map to service call.
   - Handle cases where body is absent by providing defaults.
   - Return ResponseEntity<ApiResponse<T>> with ApiResponse.success(data).
5. Extend service interface and implement in service/impl:
   - Encapsulate business logic, authorization checks, and transactions.
   - Apply defaults and validation in service methods.
   - Use repositories and projections; do not access DB directly.
6. Update repositories or projections if a new query is required.
7. Run tests and verify OpenAPI (enable dev profile).
8. Security: confirm endpoint falls under /api/v2/** (already authenticated by SecurityConfiguration).

Gotchas:
- Keep v2 responses consistently wrapped with ApiResponse<T>.
- Ensure Telegram authentication is required (no extra permitAll on the path).
- Use projection DTOs for read queries to avoid N+1 and over-fetching.
- For optional bodies, test both with and without request body to confirm defaults work.
- Ensure map fields like reviewsByRating always include keys 1-5 even if zero.

## Unified Notification Strategy Implementation (Refactored November 2025)

**Last verified**: 2025-11-23 - **COMPLETED**: Issue-84-optional-notifications

**Architecture Overview**: The notification system now uses a unified `NotificationStrategy` interface that supports both In-App and Telegram channels through shared and channel-specific methods.

Files involved:
- **Strategy Interface**: `src/main/java/me/geohod/geohodbackend/service/notification/processor/strategy/NotificationStrategy.java`
- **Strategy Registry**: `StrategyRegistry.java` - Centralized strategy selection
- **Strategy Implementations**: 
  - `EventCancelledStrategy.java`, `EventCreatedStrategy.java`, `EventFinishedStrategy.java`
  - `ParticipantRegisteredStrategy.java`, `ParticipantUnregisteredStrategy.java`
- **Processors**: `InAppNotificationProcessor.java`, `TelegramNotificationProcessor.java`
- **Templates**: Message templates with Russian text and template engine integration
- **Formatters**: `TelegramMarkdownV2Formatter.java`, `InAppFormatter.java`

**Implementation Pattern (November 2025)**:
1. **Unified Strategy Interface**: 
   ```java
   public interface NotificationStrategy {
       // Shared methods
       StrategyNotificationType getType();
       boolean isValid();
       List<User> getRecipients(EventLog eventLog);
       
       // Telegram-specific
       TelegramSendMessageParams createTelegramParams();
       String formatTelegramMessage();
       
       // In-App-specific  
       Notification createInAppNotification();
   }
   ```

2. **Strategy Registry Integration**:
   ```java
   @Component
   public class InAppNotificationProcessor {
       private final StrategyRegistry strategyRegistry;
       
       public void process(EventLog eventLog) {
           NotificationStrategy strategy = strategyRegistry.getStrategy(eventLog.getType())
               .orElseThrow(() -> new GeohodException("No strategy found"));
           // Delegate to strategy...
       }
   }
   ```

3. **Refactored Strategy Implementations**:
   - All five strategies updated to implement unified interface methods
   - Channel-specific logic isolated in appropriate methods
   - Consistent validation and recipient determination

**Benefits Achieved**:
- **Code Deduplication**: Eliminated hardcoded recipient determination and DTO creation logic
- **Maintainability**: Cleaner separation of concerns between processors and strategies
- **Consistency**: Uniform strategy selection and execution across channels
- **Extensibility**: Easy to add new notification types or channels

**Verification Results (November 2025)**:
All tests in the service.notification package passed, including updated processor tests:
```
InAppNotificationProcessorTest > testProcessWithEventCreatedLog() PASSED
InAppNotificationProcessorTest > testProcessWithNoUnprocessedLogs() PASSED
TelegramNotificationProcessorTest > testProcessCallsStrategyAndPublishesNotification(...) PASSED
TelegramNotificationProcessorTest > testProcessDoesNothingWhenNoEventLogs(...) PASSED
MessageTemplateTest > variableSubstitution() PASSED
...
BUILD SUCCESSFUL
```

**Template System** (unchanged):
- Russian-language templates with sophisticated variable interpolation
- Conditional blocks and fallback values
- Telegram MarkdownV2 formatting with proper escaping

**Templates Available** (as of November 2025):
- `event.created` - New event notifications
- `event.cancelled.organizer.notify-participants` - Event cancelled, notify participants
- `event.cancelled.organizer.not-notify-participants` - Event cancelled, don't notify
- `event.cancelled` - Event cancelled (to participants)
- `participant.registered` - Participant registered
- `participant.unregistered` - Participant unregistered  
- `event.finished` - Event finished for review

**Template Variables** (common):
- `{{eventName}}`, `{{eventDate}}`, `{{eventId}}`, `{{registrationLink}}`, `{{reviewLink}}`
- `{{authorFirstName}}`, `{{authorLastName}}`, `{{authorTgUsername}}`, `{{authorFullName}}`
- `{{contactInfo}}`, `{{contactName}}`, `{{contactLink}}`
- `{{botName}}` - From GeohodProperties

**Key Implementation Notes**:
- All templates are in Russian language
- Telegram formatting requires careful escaping of special characters
- URLs in (...) must have `)` and `\` escaped
- Plain URLs should be preserved without escaping
- Use TemplateType.TELEGRAM for all Telegram notifications

## Add or Update User Settings

**Last verified**: 2025-11-23

Files typically involved:
- Controller: src/main/java/me/geohod/geohodbackend/user_settings/api/UserSettingsController.java
- Service: src/main/java/me/geohod/geohodbackend/user_settings/service/UserSettingsServiceImpl.java
- Model: src/main/java/me/geohod/geohodbackend/user_settings/data/model/UserSettings.java
- DTOs: src/main/java/me/geohod/geohodbackend/user_settings/api/dto/...
- Repository: src/main/java/me/geohod/geohodbackend/user_settings/data/repository/UserSettingsRepository.java
- Mapper: src/main/java/me/geohod/geohodbackend/user_settings/mapper/UserSettingsMapper.java

Steps:
1. **Update Model** (UserSettings.java):
   - Add new field with appropriate Java type
   - Add update method for the field
   - Update constructors if needed

2. **Update Service Interface & Implementation**:
   - Add service method in IUserSettingsService
   - Implement in UserSettingsServiceImpl with @Transactional
   - Use getOrCreateSettings() pattern for atomic updates

3. **Update API Layer**:
   - Add specific request DTO for the field
   - Add dedicated PUT endpoint in UserSettingsController
   - Map request through UserSettingsMapper

4. **Update Mapper**:
   - Extend UserSettingsMapper for new request/response DTOs
   - Ensure bidirectional mapping between DTOs and model

5. **Test**:
   - Test service layer with repository integration
   - Test API endpoint with valid/invalid requests
   - Verify user creation and default handling

**Fields Available** (November 2025):
- `defaultDonationAmount` (String) - **Deprecated, ignored**
- `defaultMaxParticipants` (Integer) - Default participants count
- `paymentGatewayUrl` (String) - **NEW**: Payment gateway integration URL
- `showBecomeOrganizer` (Boolean) - **NEW**: Show/hide organizer advertisement

**API Endpoints**:
- `PUT /api/v2/user/settings` - Update all settings
- `PUT /api/v2/user/settings/max-participants` - Update max participants
- `PUT /api/v2/user/settings/payment-gateway-url` - **NEW**: Update payment gateway URL
- `PUT /api/v2/user/settings/show-become-organizer` - **NEW**: Update organizer ad preference

## Multi-Participant Event Registration

**Last verified**: 2025-11-23

Files involved:
- Controller: src/main/java/me/geohod/geohodbackend/api/controller/v2/EventParticipationController.java
- Service: src/main/java/me/geohod/geohodbackend/service/impl/EventParticipationService.java
- DTO: src/main/java/me/geohod/geohodbackend/api/dto/request/EventRegisterRequest.java
- Repository: EventParticipant repository and projections

Steps:
1. **API Enhancement**:
   - Optional `EventRegisterRequest` body with `amountOfParticipants` field (1-10)
   - Maintain backward compatibility for requests without body (defaults to 1)
   - Use `@RequestBody(required = false)` in controller

2. **Service Implementation**:
   - Loop creation of EventParticipant records based on amount
   - Validate total capacity against event.maxParticipants
   - Ensure all participants belong to same user (current user)
   - Transaction handling for batch creation

3. **Data Validation**:
   - Check capacity before and after adding participants
   - Ensure no duplicate registrations
   - Handle edge cases (amount=0, amount>capacity)

**Current Status**: Fully implemented and tested

## Database Migration (Liquibase)

**Last verified**: 2025-11-23

Files involved:
- Master changelog: src/main/resources/db/changelog/db.changelog-master.xml
- New changelog: src/main/resources/db/changelog/db.changelog-*.xml

Steps:
1. Create new changelog file with sequential version number
2. Define schema changes with appropriate SQL
3. Include in master changelog if needed
4. Test migration on development database

**Recent Migrations** (November 2025):
- `db.changelog-2.3-user-show-organizer-ad.xml` - Set show_become_organizer for existing users

## Testing Patterns (Enhanced November 2025)

**Testcontainers Setup**:
```java
@SpringBootTest
@Testcontainers
public abstract class AbstractIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
}
```

**Notification Strategy Testing**:
- Test template rendering with mock data
- Verify Telegram MarkdownV2 formatting
- Test fallback behavior for missing variables
- Validate conditional template expressions

**User Settings Testing**:
- Test granular API endpoints
- Verify default settings creation
- Test field-specific updates

## Enable Swagger/OpenAPI locally

**Last verified**: 2025-11-23

Steps:
1. Start local infrastructure: `docker compose up -d`
2. Run application with dev profile:
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```
3. Access:
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - OpenAPI JSON: http://localhost:8080/api-docs

**Profile Configuration**: Dev profile enables:
- springdoc.swagger-ui.enabled=true
- springdoc.api-docs.enabled=true
- Detailed actuator health information