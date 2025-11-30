# Tasks: Geohod Backend

This document captures repetitive workflows to execute similar changes quickly and consistently. Follow these steps when performing the associated tasks.

## Memory Bank Update (November 2025)

**Last verified**: November 30, 2025 - **COMPLETED**

**Overview**: Complete Memory Bank refresh to document API v3 development, security modernization, database evolution, and all recent architectural changes.

**Files involved**:
- **All Memory Bank Files**: README.md, projectBrief.md, productContext.md, activeContext.md, progress.md, systemPatterns.md, tasks.md, techContext.md

**Implementation Details**:
1. **Analysis Phase**: 
   - Comprehensive project structure analysis with focus on recent changes
   - Review of API_CONTRACT_V3.md for new features and endpoints
   - Examination of 16 Liquibase migrations for database evolution
   - Security implementation review including v1 removal and method-level security
   - Notification system analysis with unified strategy pattern

2. **Documentation Updates**:
   - **README.md**: Updated with API v3 features, 16 migrations, memory bank modernization
   - **projectBrief.md**: Enhanced with API v3 development, security modernization, database evolution
   - **productContext.md**: Added user statistics, state management, enhanced user profiles
   - **activeContext.md**: Documented API v3 development and memory bank update process
   - **progress.md**: Updated with comprehensive completion status and current development state
   - **systemPatterns.md**: Enhanced with API v3 patterns, unified strategy pattern, security modernization
   - **tasks.md**: Added memory bank update task and API v3 development tasks
   - **techContext.md**: Updated with latest dependencies, API v3 features, comprehensive architecture

3. **Consistency Verification**:
   - Ensured all files reflect current implementation state
   - Verified consistency across documentation with proper cross-references
   - Updated dates and status information throughout all files

**Benefits Achieved**:
- **Complete Documentation**: Memory Bank now accurately reflects current implementation state
- **API v3 Coverage**: Comprehensive documentation of enhanced user settings and user statistics
- **Security Modernization**: Complete coverage of v1 removal and method-level security implementation
- **Database Evolution**: Full documentation of 16 migrations including state management features
- **Developer Onboarding**: Memory Bank serves as authoritative reference for new developers

## API v3 Development and User Settings Enhancement

**Last verified**: November 30, 2025 - **COMPLETED**

**Overview**: Implementation of API v3 features including enhanced user settings with granular endpoints and comprehensive user statistics.

**Files involved**:
- **Documentation**: `API_CONTRACT_V3.md` - Complete API contract with examples
- **Controller**: `src/main/java/me/geohod/geohodbackend/api/controller/v2/UserSettingsController.java`
- **DTOs**: 
  - `src/main/java/me/geohod/geohodbackend/api/dto/request/DefaultMaxParticipantsRequest.java`
  - `src/main/java/me/geohod/geohodbackend/api/dto/request/PaymentGatewayUrlRequest.java`
  - `src/main/java/me/geohod/geohodbackend/api/dto/request/ShowBecomeOrganizerRequest.java`
  - `src/main/java/me/geohod/geohodbackend/api/dto/request/UserSettingsRequest.java` (updated)
- **Responses**: 
  - `src/main/java/me/geohod/geohodbackend/api/dto/response/UserSettingsResponse.java` (updated)
  - `src/main/java/me/geohod/geohodbackend/api/dto/response/UserStatsResponse.java` (new)
  - `src/main/java/me/geohod/geohodbackend/api/dto/response/UserDetailsResponse.java` (new)

**Implementation Details**:
1. **Enhanced User Settings API**:
   - Four dedicated PUT endpoints for granular settings updates
   - Comprehensive DTO validation with @Min, @Max, @NotNull annotations
   - Backward compatibility with existing broader update endpoint
   - Payment gateway URL and organizer advertisement support

2. **User Statistics Implementation**:
   - `GET /api/v2/users/{id}/stats` endpoint with comprehensive statistics
   - Overall rating calculation with decimal precision
   - Review distribution by rating (1-5 scale) with guaranteed key presence
   - Event participation counts (organized events, participated events)

3. **User Details Enhancement**:
   - `GET /api/v2/users/{id}` endpoint with enhanced profile information
   - Avatar image URL support and contact information display
   - Comprehensive user profile data retrieval

4. **API Documentation**:
   - Complete API_CONTRACT_V3.md with JSON examples
   - Response format specifications with proper validation examples
   - DTO documentation with field descriptions and constraints

**Benefits Achieved**:
- **Granular Control**: Users can update individual settings without affecting others
- **Comprehensive Statistics**: Detailed user profiles improve community trust and engagement
- **API Documentation**: Complete contract with examples facilitates client development
- **Validation**: Comprehensive DTO validation ensures data integrity and proper error handling

## Security Modernization and API Cleanup

**Last verified**: November 25, 2025 - **COMPLETED**

**Overview**: Complete removal of legacy v1 API endpoints and modernization of security with method-level @PreAuthorize annotations.

**Files involved**:
- **Removed**: `src/main/java/me/geohod/geohodbackend/api/controller/EventController.java` (v1 legacy controller)
- **Enhanced**: `src/main/java/me/geohod/geohodbackend/api/controller/v2/EventController.java` (v2 with @PreAuthorize)
- **Enhanced**: `src/main/java/me/geohod/geohodbackend/configuration/SecurityConfiguration.java` (@EnableMethodSecurity)
- **Added**: `src/main/java/me/geohod/geohodbackend/security/EventSecurity.java` (new authorization service)

**Implementation Details**:
1. **v1 API Removal**: 
   - Deleted legacy EventController.java that handled /api/v1/events endpoints
   - Removed all v1-specific imports and dependencies
   - Cleaned up any v1 references in configuration files

2. **Method-Level Security Implementation**:
   - Added @EnableMethodSecurity annotation to SecurityConfiguration.java
   - Replaced manual AccessDeniedException checks with @PreAuthorize annotations
   - Updated method signatures to remove redundant principal.userId() extractions

3. **EventSecurity Service Creation**:
   - Created new EventSecurity.java service with isEventAuthor() method
   - Implemented SecurityContextHolder integration for authentication access
   - Added TelegramPrincipal type checking for security validation
   - Integrated EventRepository for database-driven authorization checks

4. **Controller Modernization**:
   - Updated EventController v2 methods: updateEvent(), cancelEvent(), finishEvent()
   - Replaced imperative authorization logic with declarative @PreAuthorize annotations
   - Removed duplicate AccessDeniedException throwing code
   - Enhanced code readability and maintainability

**Benefits Achieved**:
- **Architecture Cleanup**: Removed legacy v1 API completely for cleaner codebase
- **Security Modernization**: Declarative security with method-level annotations
- **Code Reduction**: Eliminated duplicate authorization logic across multiple methods
- **Maintainability**: Centralized authorization logic in dedicated EventSecurity service
- **Future-Ready**: Modern security approach preparing for API v3 evolution

**Verification Results**: All v2 EventController methods work correctly with new @PreAuthorize security, legacy v1 endpoints removed successfully.

## SQL Optimization and Raw JSON Properties

**Last verified**: November 24, 2025 - **COMPLETED**

Files involved:
- Repository: `src/main/java/me/geohod/geohodbackend/data/model/repository/EventProjectionRepository.java`
- Service: `src/main/java/me/geohod/geohodbackend/service/impl/EventService.java`

**Implementation Details**:
1. **SQL Query Optimization**: 
   - Refactored `EventProjectionRepository.events()` method with dynamic WHERE clause construction using StringBuilder
   - Implemented Map-based parameter binding instead of individual parameter binding
   - Separated base SQL, WHERE clause, and ORDER BY for better readability and performance
   - Enhanced query structure with cleaner parameter handling

2. **JSON Serialization Enhancement**:
   - Added Jackson ObjectMapper dependency to EventService
   - Created `toJson()` method for proper event log payload serialization
   - Replaced string-based JSON formatting with structured ObjectMapper usage
   - Ensured proper JSON format for event logs: `{"authorId": "..."}`, `{"notifyParticipants": true/false}`, `{"sendPollLink": true/false}`

**Benefits Achieved**:
- **Performance**: Improved query execution through optimized parameter binding
- **Code Quality**: Better maintainability with cleaner SQL construction and proper JSON serialization
- **Data Integrity**: Proper JSON formatting prevents formatting errors in event log payloads
- **Readability**: Separated SQL components make queries easier to understand and maintain

**Verification Results**: All tests passed successfully with enhanced query performance

## Event State and Participant State Management

**Last verified**: November 24, 2025 - **COMPLETED**

Files involved:
- Migration: `src/main/resources/db/changelog/db.changelog-2.4-add-event-action-states.xml`
- API Request: `src/main/java/me/geohod/geohodbackend/api/dto/request/UpdateParticipantStateRequest.java`
- Service: `src/main/java/me/geohod/geohodbackend/service/impl/EventParticipationService.java`
- Model: `src/main/java/me/geohod/geohodbackend/data/model/Event.java`, `src/main/java/me/geohod/geohodbackend/data/model/EventParticipant.java`
- Repository: `src/main/java/me/geohod/geohodbackend/data/model/repository/EventParticipantRepository.java`, `src/main/java/me/geohod/geohodbackend/data/model/repository/EventProjectionRepository.java`
- DTOs: `src/main/java/me/geohod/geohodbackend/api/dto/response/EventDetailsResponse.java`, `src/main/java/me/geohod/geohodbackend/data/dto/EventDetailedProjection.java`
- Controller: `src/main/java/me/geohod/geohodbackend/api/controller/v2/EventParticipationController.java`

**Implementation Details**:
1. **Database Schema Enhancement**: Added 6 boolean fields across events and event_participants tables:
   - Events: `send_poll_link`, `donation_cash`, `donation_transfer`
   - Participants: `poll_link_sent`, `cash_donated`, `transfer_donated`
2. **API Implementation**: Created `UpdateParticipantStateRequest` record with state fields
3. **Service Layer**: Enhanced `EventParticipationService` with state management methods
4. **Repository Updates**: Updated repositories to support state-based operations and queries
5. **Data Transfer Objects**: Enhanced response DTOs to include state information
6. **Controller Integration**: Updated controllers to handle state management requests

**Database Migration Pattern**:
```xml
<addColumn tableName="events">
    <column name="send_poll_link" type="BOOLEAN" defaultValueBoolean="false">
        <constraints nullable="false"/>
    </column>
    <column name="donation_cash" type="BOOLEAN" defaultValueBoolean="false">
        <constraints nullable="false"/>
    </column>
    <column name="donation_transfer" type="BOOLEAN" defaultValueBoolean="false">
        <constraints nullable="false"/>
    </column>
</addColumn>

<addColumn tableName="event_participants">
    <column name="poll_link_sent" type="BOOLEAN" defaultValueBoolean="false">
        <constraints nullable="false"/>
    </column>
    <column name="cash_donated" type="BOOLEAN" defaultValueBoolean="false">
        <constraints nullable="false"/>
    </column>
    <column name="transfer_donated" type="BOOLEAN" defaultValueBoolean="false">
        <constraints nullable="false"/>
    </column>
</addColumn>
```

**Verification Results**: All database migrations applied successfully with proper default values

## Event Sorting Implementation

**Last verified**: November 23, 2025 - **COMPLETED**

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

Last verified: November 23, 2025

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

**Last verified**: November 23, 2025 - **COMPLETED**: Unified Strategy Pattern

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
       boolean isValid(Event event, String payload);
       Collection<UUID> getRecipients(Event event, String payload);
       
       // Telegram-specific
       Map<String, Object> createTelegramParams(Event event, String payload);
       String formatTelegramMessage(Event event, User author, Map<String, Object> params);
       
       // In-App-specific  
       NotificationCreateDto createInAppNotification(UUID userId, Event event, String payload);
   }
   ```

2. **Strategy Registry Integration**:
   ```java
   @Component
   public class InAppNotificationProcessor {
       private final StrategyRegistry strategyRegistry;
       
       @Scheduled(fixedDelayString = "${geohod.processor.in-app.delay:5000}")
       @Transactional
       public void process() {
           List<EventLog> unprocessedLogs = eventLogService.findUnprocessed(100, PROCESSOR_NAME);
           // Process logs with strategy delegation...
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

**Last verified**: November 23, 2025 - **Enhanced November 2025**

Files typically involved:
- Controller: src/main/java/me/geohod/geohodbackend/api/controller/v2/UserSettingsController.java
- Service: src/main/java/me/geohod/geohodbackend/service/impl/UserSettingsServiceImpl.java
- Model: src/main/java/me/geohod/geohodbackend/data/model/UserSettings.java
- DTOs: 
  - Request: src/main/java/me/geohod/geohodbackend/api/dto/request/DefaultMaxParticipantsRequest.java (new)
  - Request: src/main/java/me/geohod/geohodbackend/api/dto/request/PaymentGatewayUrlRequest.java (new)
  - Request: src/main/java/me/geohod/geohodbackend/api/dto/request/ShowBecomeOrganizerRequest.java (new)
  - Request: src/main/java/me/geohod/geohodbackend/api/dto/request/UserSettingsRequest.java (enhanced)
- Repository: src/main/java/me/geohod/geohodbackend/data/model/repository/UserSettingsRepository.java
- Mapper: src/main/java/me/geohod/geohodbackend/api/mapper/UserSettingsApiMapper.java

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
   - Add specific request DTO for the field (API v3 pattern)
   - Add dedicated PUT endpoint in UserSettingsController
   - Map request through UserSettingsApiMapper

4. **Update Mapper**:
   - Extend UserSettingsApiMapper for new request/response DTOs
   - Ensure bidirectional mapping between DTOs and model

5. **Test**:
   - Test service layer with repository integration
   - Test API endpoint with valid/invalid requests
   - Verify user creation and default handling

**Fields Available** (November 2025):
- `defaultDonationAmount` (String) - **Deprecated, ignored**
- `defaultMaxParticipants` (Integer) - Default participants count
- `paymentGatewayUrl` (String) - **API v3**: Payment gateway integration URL
- `showBecomeOrganizer` (Boolean) - **API v3**: Show/hide organizer advertisement

**API v3 Endpoints**:
- `PUT /api/v2/user/settings` - Update all settings (enhanced)
- `PUT /api/v2/user/settings/default-max-participants` - **NEW**: Update max participants
- `PUT /api/v2/user/settings/payment-gateway-url` - **NEW**: Update payment gateway URL
- `PUT /api/v2/user/settings/show-become-organizer` - **NEW**: Update organizer ad preference

## Multi-Participant Event Registration

**Last verified**: November 23, 2025

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

**Current Status**: Fully implemented and tested with comprehensive validation

## Database Migration (Liquibase)

**Last verified**: November 23, 2025 - **Enhanced with 16 migrations**

Files involved:
- Master changelog: src/main/resources/db/changelog/db.changelog-master.xml
- New changelog: src/main/resources/db/changelog/db.changelog-*.xml (16 total)

Steps:
1. Create new changelog file with sequential version number
2. Define schema changes with appropriate SQL
3. Include in master changelog if needed
4. Test migration on development database

**Recent Migrations (November 2025)**:
- `db.changelog-2.0-performance-indexes.xml` - Performance optimization indexes
- `db.changelog-2.1-user_settings_enhancement.xml` - Payment gateway URL and organizer advertisement
- `db.changelog-2.2-remove-unique-constraint.xml` - Constraint adjustments
- `db.changelog-2.3-user-show-organizer-ad.xml` - Set show_become_organizer for existing users
- `db.changelog-2.4-add-event-action-states.xml` - Event and participant state management

**Migration Patterns**:
- **Idempotent**: All changes safe to re-run
- **Default Values**: Proper defaults for new fields (false for boolean, null for optional)
- **Data Migration**: Existing data handling with backward compatibility
- **Index Strategy**: Performance-oriented indexing

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
- Test unified strategy interface implementations

**User Settings Testing**:
- Test granular API v3 endpoints
- Verify default settings creation
- Test field-specific updates
- Validate DTO validation and error handling

**State Management Testing**:
- Test event and participant state transitions
- Verify atomic state updates
- Validate state display in API responses
- Test state management with different user scenarios

**Security Testing**:
- Test method-level security with @PreAuthorize
- Verify EventSecurity service functionality
- Test v1 API removal (endpoints should not exist)
- Validate authorization edge cases

## Enable Swagger/OpenAPI locally

**Last verified**: November 23, 2025

Steps:
1. Start local infrastructure: `docker compose up -d`
2. Run application with dev profile:
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```
3. Access:
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - OpenAPI JSON: http://localhost:8080/api-docs
   - API v3 Contract: API_CONTRACT_V3.md (in project root)

**Profile Configuration**: Dev profile enables:
- springdoc.swagger-ui.enabled=true
- springdoc.api-docs.enabled=true
- Detailed actuator health information
- Enhanced logging and debugging features

## Performance Optimization Workflows

### SQL Query Optimization
1. **Dynamic Query Construction**: Use StringBuilder for WHERE clauses
2. **Parameter Binding**: Implement Map-based named parameters
3. **Query Separation**: Separate base SQL, WHERE, and ORDER BY clauses
4. **Performance Testing**: Use Testcontainers for realistic performance testing

### JSON Processing Enhancement
1. **Jackson Integration**: Add ObjectMapper for proper serialization
2. **Error Handling**: Implement proper exception handling for JSON failures
3. **Consistency**: Use uniform JSON formatting across the application
4. **Validation**: Test JSON serialization with various data scenarios

### Database Indexing Strategy
1. **Performance Analysis**: Identify slow queries and access patterns
2. **Strategic Indexing**: Add indexes for frequently queried fields
3. **Migration Safety**: Ensure indexes don't impact existing functionality
4. **Monitoring**: Track query performance improvements after indexing