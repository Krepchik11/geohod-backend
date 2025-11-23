# Tasks: Geohod Backend

This document captures repetitive workflows to execute similar changes quickly and consistently. Follow these steps when performing the associated tasks.

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

## Implement Notification Strategy (New November 2025)

**Last verified**: 2025-11-23 - Critical for Issue-84-optional-notifications

Files typically involved:
- Strategy: src/main/java/me/geohod/geohodbackend/service/notification/processor/strategy/...
- Message Templates: src/main/java/me/geohod/geohodbackend/service/notification/processor/strategy/message/...
- Template Registry: MessageTemplateRegistry with @PostConstruct initialization
- Message Formatter: MessageFormatter with channel-specific formatting

Steps:
1. **Create Strategy Implementation**:
   - Extend `NotificationStrategy` interface
   - Implement required methods: `createParams()`, `getRecipients()`, `formatMessage()`, `getType()`, `isValid()`
   - Register in `NotificationStrategyConfiguration` @PostConstruct method

2. **Define Template**:
   - Create MessageTemplate with ID, Russian text, TemplateType
   - Support variables: {{eventName}}, {{eventDate}}, {{contactInfo}}, {{registrationLink}}, etc.
   - Add fallbacks for missing data
   - Register in MessageTemplateRegistry.initializeDefaultTemplates()

3. **Implement Message Formatting**:
   - Ensure TelegramMarkdownV2Formatter properly escapes content
   - Handle special characters: `*`, `_`, `[`, `]`, `(`, `)`, `~`, `` ` ``, `>`, `#`, `+`, `=`, `|`, `{`, `}`, `!`
   - Preserve plain URLs while escaping markdown formatting
   - Support template expressions: `{{variable}}`, `{{variable|fallback}}`, `{{variable:50}}`

4. **Template Engine Integration**:
   - Support conditional blocks: `{#if condition}content{/if}`
   - Variable interpolation with fallbacks and length limits
   - Automatic data context building from events and users

5. **Test Strategy**:
   - Create dedicated test class for the strategy
   - Test template rendering with various data combinations
   - Verify Telegram MarkdownV2 formatting works correctly
   - Test fallback handling for missing variables

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