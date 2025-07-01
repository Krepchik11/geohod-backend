# User Settings Technical Specification

## 0\. System Overview

### Core purpose and value proposition

The core purpose of this feature is to enable registered event organizers to personalize and streamline the event creation process. By allowing users to set default values for donation amounts and maximum participant counts, the system will pre-fill these fields in the frontend event creation form, reducing manual input and enhancing the user experience. This feature adds value by offering personalized efficiency and flexibility to event organizers, making it easier and quicker to set up new events.

### Key workflows

1.  **Retrieve User Settings:** A registered user requests their current default event settings (donation amount, max participants).
      * The system authenticates the user.
      * The system fetches the `UserSettings` associated with the user's ID.
      * If no settings exist, default `null`/empty values are returned.
      * The settings are returned to the frontend.
2.  **Update User Settings:** A registered user submits new default event settings.
      * The system authenticates the user.
      * The system receives the desired `default_donation_amount` and `default_max_participants`.
      * The system creates or updates the `UserSettings` entry for the user.
      * The updated settings are returned to the frontend.
3.  **Event Creation Pre-fill (Frontend Interaction):**
      * The frontend calls the backend API to retrieve the current user's default settings.
      * These retrieved values are then used to pre-populate the corresponding fields in the event creation form.
      * Users can override these pre-filled values as needed during event creation.

### System architecture

The feature will be implemented within the existing Spring Boot backend application, following a layered architecture:

  * **Controller Layer:** RESTful API endpoints for handling HTTP requests (GET and PUT) related to user settings. It will interact with the Service Layer.
  * **Service Layer:** Contains the business logic for managing user settings, including fetching and updating. It will interact with the Repository Layer.
  * **Repository Layer:** Interacts directly with the database using Spring Data JDBC to perform CRUD operations on the `UserSettings` entity.
  * **Model/Entity Layer:** Represents the `user_settings` table in the database.
  * **DTO Layer:** Data Transfer Objects for request and response payloads, ensuring clear data contracts between the frontend and backend.

## 1\. Project Structure

The new feature components will be organized under a dedicated package structure to maintain modularity and alignment with existing project patterns.

```
src/main/java/me/geohod/geohodbackend/
├── user_settings/
│   ├── api/
│   │   ├── UserSettingsController.java
│   │   ├── dto/
│   │   │   ├── UserSettingsRequest.java (record)
│   │   │   └── UserSettingsResponse.java (record)
│   ├── data/
│   │   ├── model/
│   │   │   └── UserSettings.java (entity)
│   │   └── repository/
│   │       └── UserSettingsRepository.java (Spring Data JDBC repository)
│   ├── service/
│   │   ├── IUserSettingsService.java (interface)
│   │   └── UserSettingsServiceImpl.java (implementation)
│   └── mapper/
│       └── UserSettingsMapper.java (MapStruct interface)
└── config/
    └── SecurityConfig.java (existing, for protected routes)
└── ... (existing packages like event, notification, user, etc.)
```

## 2\. Feature Specification

### 2.1 User Settings Management

#### User story and requirements

As an event organizer, I want to set default donation and participant amounts so that event creation is streamlined and personalized.

  * Users should be able to specify a default donation amount.
  * Users should be able to specify a default maximum number of participants.
  * These settings should be persistently stored for each user, linked by their unique user ID.
  * Initial State: Default values for `default donation amount` and `default maximum participants` should be `null`/empty until explicitly set by the user.

#### Detailed implementation steps

1.  **`UserSettings` Entity (`me.geohod.geohodbackend.user_settings.data.model.UserSettings.java`):**

      * Define a Java class representing the `user_settings` table.
      * It will have fields for `user_id` (UUID), `default_donation_amount` (String, nullable), and `default_max_participants` (Integer, nullable).
      * Use appropriate annotations for Spring Data JDBC.
      * Include constructors (all-args, no-args), getters, setters, equals, hashCode, and toString methods.

2.  **`UserSettingsRepository` (`me.geohod.geohodbackend.user_settings.data.repository.UserSettingsRepository.java`):**

      * An interface extending `org.springframework.data.repository.CrudRepository` for basic CRUD operations.
      * Include a method `findByUserId(UUID userId)` to retrieve settings by user ID.

3.  **`IUserSettingsService` and `UserSettingsServiceImpl` (`me.geohod.geohodbackend.user_settings.service.*`):**

      * **`IUserSettingsService`:** Defines the contract for user settings operations with methods `getUserSettings(UUID userId)` and `updateUserSettings(UUID userId, UserSettingsRequest userSettingsRequest)`.
      * **`UserSettingsServiceImpl`:** Implements the `IUserSettingsService` interface.
          * Uses constructor injection for `UserSettingsRepository` and `UserSettingsMapper`.
          * **`getUserSettings(UUID userId)`:**
              * Fetches `UserSettings` by `userId` from the repository.
              * If not found, returns a `UserSettingsResponse` with `null` for donation and participants.
              * Maps the `UserSettings` entity to `UserSettingsResponse` DTO.
          * **`updateUserSettings(UUID userId, UserSettingsRequest userSettingsRequest)`:**
              * Checks if `UserSettings` already exist for the user.
              * If exists, updates the existing entity with values from `userSettingsRequest`.
              * If not exists, creates a new `UserSettings` entity with the `userId` and provided values.
              * Saves the `UserSettings` entity using the repository.
              * Maps the saved entity to `UserSettingsResponse` DTO and returns it.

#### Error handling and edge cases

  * **User Not Authenticated:** Handled by existing Spring Security configuration. Attempts to access endpoints without proper authentication will result in 401 Unauthorized.
  * **Database Operations:** Standard Spring Data JDBC exception handling will apply. Specific error logging should be implemented in the service layer for database failures (e.g., `DataAccessException`).
  * **No specific validation:** As per the request, no specific validation (min/max, data type checks beyond what Java/database types enforce) is required for input values. `Integer` will handle numeric formats for participants, and `null` values are explicitly allowed for both fields. `String` for donation amount will accept any text.

### 2.2 API Endpoints

#### User story and requirements

  * An API endpoint to retrieve a user's default settings using the `GET` HTTP method.
  * An API endpoint to update a user's default settings using the `PUT` HTTP method.
  * These settings are general user preferences applicable across all events the user creates.
  * Payload Structure: The request/response payload structure for these API calls should align with existing patterns in the project code (e.g., DTOs/Records).

#### Detailed implementation steps

1.  **`UserSettingsRequest` DTO (`me.geohod.geohodbackend.user_settings.api.dto.UserSettingsRequest.java`):**

      * A Java record for the PUT request body with fields `defaultDonationAmount` (String) and `defaultMaxParticipants` (Integer).

2.  **`UserSettingsResponse` DTO (`me.geohod.geohodbackend.user_settings.api.dto.UserSettingsResponse.java`):**

      * A Java record for the GET response and PUT response body with fields `defaultDonationAmount` (String) and `defaultMaxParticipants` (Integer).

3.  **`UserSettingsMapper` (`me.geohod.geohodbackend.user_settings.mapper.UserSettingsMapper.java`):**

      * MapStruct interface for mapping between `UserSettings` entity and `UserSettingsResponse` DTO.
      * Annotated with `@Mapper(config = GlobalMapperConfig.class)` if a global MapStruct configuration exists.
      * Include a method `toResponse(UserSettings userSettings)`.

4.  **`UserSettingsController` (`me.geohod.geohodbackend.user_settings.api.UserSettingsController.java`):**

      * A Spring `@RestController` class annotated with `@RequestMapping("/api/user-settings")`.
      * Uses constructor injection for `IUserSettingsService`.
      * **GET `/api/user-settings`:**
          * Method signature: `public ResponseEntity<UserSettingsResponse> getUserSettings(@AuthenticationPrincipal TelegramPrincipal principal)`.
          * Retrieves the current authenticated user's ID from `principal`.
          * Calls `IUserSettingsService.getUserSettings(userId)`.
          * Returns `UserSettingsResponse` with HTTP status 200 OK.
      * **PUT `/api/user-settings`:**
          * Method signature: `public ResponseEntity<UserSettingsResponse> updateUserSettings(@AuthenticationPrincipal TelegramPrincipal principal, @RequestBody UserSettingsRequest request)`.
          * Retrieves the current authenticated user's ID from `principal`.
          * Accepts `UserSettingsRequest` as `@RequestBody`.
          * Calls `IUserSettingsService.updateUserSettings(userId, request)`.
          * Returns `UserSettingsResponse` of the updated settings with HTTP status 200 OK.

#### Error handling and edge cases

  * **Invalid Request Body:** Spring will automatically handle malformed JSON (e.g., `HttpMessageNotReadableException`) resulting in 400 Bad Request.
  * **Security Context:** If `principal` is null (should not happen with `@AuthenticationPrincipal` on protected endpoints), it indicates a security configuration issue.
  * **Internal Server Errors:** Any unhandled exceptions from the service or repository layers will propagate up and should be caught by a global exception handler (`@ControllerAdvice`) if one exists, returning 500 Internal Server Error.

## 3\. Database Schema

### 3.1 Tables

#### Table: `user_settings`

  * **Purpose:** Stores personalized default settings for each user.
  * **Compact Schema:**
      * `user_id` UUID PRIMARY KEY REFERENCES users(id)
      * `default_donation_amount` VARCHAR(255) NULL
      * `default_max_participants` INTEGER NULL
  * **Relationships and Indexes:**
      * **Relationship:** One-to-one relationship with the `users` table via `user_id`.
      * **Indexes:** Primary key index on `user_id`.

## 4\. Server Actions

### 4.1 Database Actions

These actions are performed by the `UserSettingsRepository`.

  * **`save(UserSettings userSettings)`:**
      * **Description:** Inserts a new `UserSettings` record or updates an existing one based on the `user_id`.
      * **Input Parameters:** `UserSettings` entity.
      * **Return Values:** The saved `UserSettings` entity.
  * **`findByUserId(UUID userId)`:**
      * **Description:** Retrieves `UserSettings` for a specific user.
      * **Input Parameters:** `UUID userId`.
      * **Return Values:** `Optional<UserSettings>` containing the settings if found, or empty if not.

### 5.2 Other Actions

These actions are performed by the `UserSettingsServiceImpl`.

  * **`getUserSettings(UUID userId)`:**
      * **Description:** Retrieves the default settings for a given user. If settings do not exist, it returns a DTO with `null` values.
      * **Input Parameters:** `UUID userId` (obtained from `AuthenticationPrincipal`).
      * **Return Values:** `UserSettingsResponse` DTO containing `defaultDonationAmount` and `defaultMaxParticipants`.
  * **`updateUserSettings(UUID userId, UserSettingsRequest userSettingsRequest)`:**
      * **Description:** Creates or updates the default settings for a given user based on the provided request.
      * **Input Parameters:** `UUID userId` (obtained from `AuthenticationPrincipal`), `UserSettingsRequest` DTO.
      * **Return Values:** `UserSettingsResponse` DTO reflecting the newly updated or created settings.

## 6\. Authentication & Authorization

  * **Implementation Details:**
      * User identity will be established using the existing `SecurityContext`. The `TelegramPrincipal` (which contains `userId`) will be injected directly into controller methods using `@AuthenticationPrincipal`.
      * This ensures that only an authenticated user can access their own settings.
  * **Protected routes configuration:**
      * The `/api/user-settings` endpoint (both GET and PUT) should be protected to require authentication. This can be configured in `SecurityConfig.java` using Spring Security by adding `requestMatchers("/api/user-settings").authenticated()`.
  * **Session management strategy:** The existing session management (stateless, token-based authentication with `TelegramTokenAuthenticationFilter`) will be leveraged. No new session management is required.

## 7\. Data Flow

  * **Server/client data passing mechanisms:**
      * **Client to Server (Update):** Frontend sends a `PUT` request to `/api/user-settings` with a JSON payload representing `UserSettingsRequest`. The controller receives this, extracts the `userId` from the authenticated principal, and passes the DTO and userId to the service. The service updates the entity via the repository.
      * **Server to Client (Retrieve/Update Response):** The service retrieves or saves the `UserSettings` entity, maps it to a `UserSettingsResponse` DTO, which the controller then sends back as a JSON response to the frontend.
  * **State management architecture:**
      * User settings are stateful and managed persistently in the PostgreSQL database.
      * The backend serves as the single source of truth for user settings.
      * The frontend will fetch these settings on demand (e.g., when the event creation form loads or the user settings page is accessed).

## 12\. Testing

Comprehensive unit tests will be written for the newly implemented components, focusing on isolated logic and interactions.

### Unit tests

  * **`UserSettingsServiceTest`:**

      * **Purpose:** To verify the business logic within `UserSettingsServiceImpl`.
      * **Dependencies to Mock:** `UserSettingsRepository`, `UserSettingsMapper`.
      * **Test Cases:**
          * Verify correct retrieval and mapping when settings exist.
          * Verify that `null` values are returned in the DTO when settings do not exist.
          * Verify that existing settings are updated correctly.
          * Verify that new settings are created when they don't exist for a user.
          * Test cases for `null` handling in requests/responses, ensuring `null` values are correctly persisted and retrieved for `default_donation_amount` and `default_max_participants`.

  * **`UserSettingsControllerTest`:**

      * **Purpose:** To verify the API endpoints, request/response handling, and interaction with the service layer.
      * **Dependencies to Mock:** `IUserSettingsService`.
      * **Test Cases (using Spring MockMvc):**
          * Test GET request returns 200 OK with correct `UserSettingsResponse`. Simulate authenticated user.
          * Test PUT request returns 200 OK with updated `UserSettingsResponse`. Verify service method call with correct arguments. Simulate authenticated user.
          * Test GET request without authentication returns 401 Unauthorized.
          * Test PUT request without authentication returns 401 Unauthorized.

  * **Database Migration Test (Manual/Integration):**

      * A manual or automated integration test (e.g., using Testcontainers) to ensure the Flyway/Liquibase migration script correctly creates the `user_settings` table with the specified schema and constraints.