# Implementation Plan

## Project Setup and Core Data Model
- [x] Step 1: Create Base Package Structure
  - **Task**: Create the necessary directory structure for the new `user_settings` feature within `src/main/java/me/geohod/geohodbackend/`. This includes `user_settings/api`, `user_settings/api/dto`, `user_settings/data/model`, `user_settings/data/repository`, `user_settings/service`, and `user_settings/mapper`. This step sets up the modular organization for the new feature's components.
  - **Files**:
    - `src/main/java/me/geohod/geohodbackend/user_settings/api/` (directory)
    - `src/main/java/me/geohod/geohodbackend/user_settings/api/dto/` (directory)
    - `src/main/java/me/geohod/geohodbackend/user_settings/data/model/` (directory)
    - `src/main/java/me/geohod/geohodbackend/user_settings/data/repository/` (directory)
    - `src/main/java/me.geohod/geohodbackend/user_settings/service/` (directory)
    - `src/main/java/me/geohod/geohodbackend/user_settings/mapper/` (directory)
  - **Step Dependencies**: None
  - **User Instructions**: No specific user action is required for this step.

- [x] Step 2: Implement UserSettings Entity
  - **Task**: Create the `UserSettings.java` class. This class will serve as the Spring Data JDBC entity for the `user_settings` table, defining fields for `user_id` (UUID), `default_donation_amount` (String, nullable), and `default_max_participants` (Integer, nullable). Ensure appropriate Spring Data JDBC annotations (e.g., `@Table`, `@Id`) are used, along with constructors (all-args, no-args), getters, setters, `equals()`, `hashCode()`, and `toString()` methods.
  - **Files**:
    - `src/main/java/me/geohod/geohodbackend/user_settings/data/model/UserSettings.java`: New file for the UserSettings entity.
  - **Step Dependencies**: Step 1
  - **User Instructions**: No specific user action is required for this step.

- [x] Step 3: Implement UserSettingsRepository
  - **Task**: Create the `UserSettingsRepository.java` interface. This interface will extend `org.springframework.data.repository.CrudRepository<UserSettings, UUID>` to provide basic CRUD operations for the `UserSettings` entity. Additionally, define a custom method `Optional<UserSettings> findByUserId(UUID userId)` to allow retrieval of user settings by their unique user ID.
  - **Files**:
    - `src/main/java/me/geohod/geohodbackend/user_settings/data/repository/UserSettingsRepository.java`: New file for the UserSettings repository.
  - **Step Dependencies**: Step 1, Step 2
  - **User Instructions**: No specific user action is required for this step.

## API Layer Implementation - DTOs and Mapper
- [x] Step 4: Create UserSettingsRequest DTO
  - **Task**: Create the `UserSettingsRequest.java` as a Java record in the DTO package. This record will define the structure for incoming PUT requests to update user settings, including fields for `defaultDonationAmount` (String) and `defaultMaxParticipants` (Integer). These fields should be nullable to support the initial empty state and partial updates.
  - **Files**:
    - `src/main/java/me/geohod/geohodbackend/user_settings/api/dto/UserSettingsRequest.java`: New file for the UserSettingsRequest DTO.
  - **Step Dependencies**: Step 1
  - **User Instructions**: No specific user action is required for this step.

- [x] Step 5: Create UserSettingsResponse DTO
  - **Task**: Create the `UserSettingsResponse.java` as a Java record in the DTO package. This record will define the structure for responses from GET and PUT API calls related to user settings, including fields for `defaultDonationAmount` (String) and `defaultMaxParticipants` (Integer).
  - **Files**:
    - `src/main/java/me/geohod/geohodbackend/user_settings/api/dto/UserSettingsResponse.java`: New file for the UserSettingsResponse DTO.
  - **Step Dependencies**: Step 1
  - **User Instructions**: No specific user action is required for this step.

- [x] Step 6: Implement UserSettingsMapper
  - **Task**: Create the `UserSettingsMapper.java` interface. This will be a MapStruct mapper responsible for converting between the `UserSettings` entity and the `UserSettingsResponse` DTO. Annotate the interface with `@Mapper(config = GlobalMapperConfig.class)` (assuming `GlobalMapperConfig` exists for shared MapStruct configurations) and include a method `UserSettingsResponse toResponse(UserSettings userSettings)`.
  - **Files**:
    - `src/main/java/me/geohod/geohodbackend/user_settings/mapper/UserSettingsMapper.java`: New file for the UserSettings MapStruct mapper.
  - **Step Dependencies**: Step 1, Step 2, Step 5
  - **User Instructions**: Ensure that the `GlobalMapperConfig.class` exists in your project. If it doesn't, you might need to create a simple placeholder or remove the `config` attribute from the `@Mapper` annotation initially.

## Service Layer Implementation
- [ ] Step 7: Implement IUserSettingsService Interface
  - **Task**: Create the `IUserSettingsService.java` interface. This interface will define the contract for the user settings business logic. It should declare two methods: `UserSettingsResponse getUserSettings(UUID userId)` to retrieve a user's settings, and `UserSettingsResponse updateUserSettings(UUID userId, UserSettingsRequest userSettingsRequest)` to create or update a user's settings.
  - **Files**:
    - `src/main/java/me/geohod/geohodbackend/user_settings/service/IUserSettingsService.java`: New file for the UserSettings service interface.
  - **Step Dependencies**: Step 1, Step 4, Step 5
  - **User Instructions**: No specific user action is required for this step.

- [ ] Step 8: Implement UserSettingsServiceImpl
  - **Task**: Create the `UserSettingsServiceImpl.java` class, which will implement the `IUserSettingsService` interface. Use constructor injection for `UserSettingsRepository` and `UserSettingsMapper`.
    - For `getUserSettings(UUID userId)`: Fetch `UserSettings` from the repository by `userId`. If settings are not found, return a `UserSettingsResponse` with `null` values for donation and participants. Otherwise, map the found entity to `UserSettingsResponse`.
    - For `updateUserSettings(UUID userId, UserSettingsRequest userSettingsRequest)`: Check if `UserSettings` already exist for the user. If they do, update the existing entity with values from `userSettingsRequest`. If not, create a new `UserSettings` entity with the provided `userId` and request values. Save the entity using the repository and then map the saved entity to `UserSettingsResponse` before returning.
  - **Files**:
    - `src/main/java/me/geohod/geohodbackend/user_settings/service/UserSettingsServiceImpl.java`: New file for the UserSettings service implementation.
  - **Step Dependencies**: Step 1, Step 3, Step 4, Step 5, Step 6, Step 7
  - **User Instructions**: No specific user action is required for this step.

## API Layer Implementation - Controller
- [ ] Step 9: Implement UserSettingsController
  - **Task**: Create the `UserSettingsController.java` class. This will be a Spring `@RestController` handling API requests for user settings, annotated with `@RequestMapping("/api/user-settings")`. Implement constructor injection for `IUserSettingsService`.
    - For the GET endpoint (`GET /api/user-settings`): Define a method `public ResponseEntity<UserSettingsResponse> getUserSettings(@AuthenticationPrincipal TelegramPrincipal principal)`. Retrieve the authenticated user's ID from the `TelegramPrincipal`, call `IUserSettingsService.getUserSettings(userId)`, and return the `UserSettingsResponse` with HTTP status 200 OK.
    - For the PUT endpoint (`PUT /api/user-settings`): Define a method `public ResponseEntity<UserSettingsResponse> updateUserSettings(@AuthenticationPrincipal TelegramPrincipal principal, @RequestBody UserSettingsRequest request)`. Retrieve the authenticated user's ID, accept `UserSettingsRequest` as `@RequestBody`, call `IUserSettingsService.updateUserSettings(userId, request)`, and return the updated `UserSettingsResponse` with HTTP status 200 OK.
  - **Files**:
    - `src/main/java/me/geohod/geohodbackend/user_settings/api/UserSettingsController.java`: New file for the UserSettings REST controller.
  - **Step Dependencies**: Step 1, Step 4, Step 5, Step 7, Step 8
  - **User Instructions**: Ensure that the `TelegramPrincipal` class is correctly defined and accessible in your project for `@AuthenticationPrincipal` to function as intended.

## Security Configuration Update
- [ ] Step 10: Update SecurityConfig.java
  - **Task**: Modify the existing `SecurityConfig.java` file (located in `src/main/java/me/geohod/geohodbackend/config/`) to protect the new `/api/user-settings` endpoint. This involves adding a rule to the security configuration that requires authentication for all requests to this path using `requestMatchers("/api/user-settings").authenticated()`.
  - **Files**:
    - `src/main/java/me/geohod/geohodbackend/config/SecurityConfig.java`: Modify existing file to add new security rules.
  - **Step Dependencies**: Step 9
  - **User Instructions**: Verify the exact location and structure of your `SecurityConfig.java` to ensure the new rule is added correctly within the existing security chain.

## Testing
- [ ] Step 11: Implement UserSettingsServiceTest
  - **Task**: Create `UserSettingsServiceTest.java` in `src/test/java/me/geohod/geohodbackend/user_settings/service/`. This unit test class will use `@ExtendWith(MockitoExtension.class)` to mock `UserSettingsRepository` and `UserSettingsMapper`. Write comprehensive test cases to verify the business logic of `UserSettingsServiceImpl`, including:
    - Correct retrieval and mapping when user settings exist.
    - Returning `null` values in the DTO when settings do not exist for a user.
    - Correct updating of existing settings.
    - Proper creation of new settings when none exist for a user.
    - Correct handling of `null` values in requests/responses (ensuring persistence and retrieval).
  - **Files**:
    - `src/test/java/me/geohod/geohodbackend/user_settings/service/UserSettingsServiceTest.java`: New file for UserSettings service unit tests.
  - **Step Dependencies**: Step 8
  - **User Instructions**: No specific user action is required for this step.

- [ ] Step 12: Implement UserSettingsControllerTest
  - **Task**: Create `UserSettingsControllerTest.java` in `src/test/java/me/geohod/geohodbackend/user_settings/api/`. This test class will use Spring MockMvc to test the API endpoints. Mock the `IUserSettingsService` dependency. Write test cases for:
    - Verifying that a GET request to `/api/user-settings` returns HTTP status 200 OK with the correct `UserSettingsResponse` for an authenticated user.
    - Verifying that a PUT request to `/api/user-settings` returns HTTP status 200 OK with the updated `UserSettingsResponse` and that the service method is called with the correct arguments for an authenticated user.
    - Testing that GET and PUT requests to `/api/user-settings` without proper authentication return HTTP status 401 Unauthorized.
  - **Files**:
    - `src/test/java/me/geohod/geohodbackend/user_settings/api/UserSettingsControllerTest.java`: New file for UserSettings controller unit tests.
  - **Step Dependencies**: Step 9
  - **User Instructions**: No specific user action is required for this step.

## Database Migration
- [ ] Step 13: Create Database Migration Script
  - **Task**: Create a new database migration script (e.g., using Flyway or Liquibase). For Flyway, the filename should follow the convention `V<timestamp>__create_user_settings_table.sql` and be placed in `src/main/resources/db/migration/`. The script should define the `CREATE TABLE user_settings` statement with the following columns: `user_id` UUID PRIMARY KEY REFERENCES `users(id)`, `default_donation_amount` VARCHAR(255) NULL, and `default_max_participants` INTEGER NULL.
  - **Files**:
    - `src/main/resources/db/migration/V<timestamp>__create_user_settings_table.sql`: New file for database migration. (Replace `<timestamp>` with an appropriate timestamp).
  - **Step Dependencies**: Step 2
  - **User Instructions**: Identify your project's chosen database migration tool (Flyway or Liquibase) and adjust the file path and naming convention accordingly. Ensure the migration script is correctly placed to be picked up by the tool on application startup.

---

## Summary of Overall Approach

The overall approach for implementing the Event Management System's User Settings feature is a layered, step-by-step methodology, moving from foundational data structures to business logic, API exposure, security integration, and comprehensive testing. We start by defining the core `UserSettings` entity and its repository, which establishes the data persistence layer. Following this, the Data Transfer Objects (DTOs) and a MapStruct mapper are introduced to standardize data contracts for API communication. The service layer then encapsulates the core business logic for retrieving and updating user settings, handling initial null states and existing settings. Subsequently, the RESTful API controller is implemented to expose these functionalities, leveraging Spring Security's `@AuthenticationPrincipal` for user identity. The existing `SecurityConfig` is then updated to protect these new endpoints. Finally, robust unit tests are designed for both the service and controller layers to ensure the correctness and reliability of the implementation. A crucial database migration step is included to create the necessary `user_settings` table.

### Key Considerations

* **Modularity**: The plan emphasizes organizing the new feature into a dedicated package structure (`user_settings`), promoting maintainability and adherence to the Single Responsibility Principle.
* **Authentication**: Leveraging the existing `TelegramPrincipal` and Spring Security ensures that user identity is correctly established and that only authenticated users can access and modify their own settings.
* **Null Handling**: Explicit attention is given to handling `null` values for `default_donation_amount` and `default_max_participants` at the entity, DTO, and service levels, aligning with the "Initial State" requirement.
* **Testing**: A strong focus on unit testing for both service and controller layers is integrated throughout the plan, ensuring thorough validation of business logic and API behavior.
* **Idempotency**: The `PUT` endpoint for updating settings inherently supports idempotency, which is a good practice for RESTful APIs.
* **Spring Boot Best Practices**: The plan consistently adheres to Spring Boot conventions, including constructor injection, RESTful API design, and appropriate use of Spring Data JDBC.