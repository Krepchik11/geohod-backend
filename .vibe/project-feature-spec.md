# Technical Specification

## 1. System Overview

-   **Core purpose and value proposition:** Enhancing an existing event management web application by integrating robust user feedback mechanisms (ratings and reviews) and a decoupled, real-time in-app notification system. These additions will improve user engagement, provide valuable insights into event quality and user performance (including organizers), and keep users informed about critical event updates and interactions. The notification architecture is designed for greater flexibility and scalability, allowing various notification channels to process system events independently.
-   **Key workflows:**
    1.  **Event Review Submission:** A concluded event triggers a Telegram notification to participants with a review link. An authenticated participant submits a review via API. The backend validates eligibility, saves the review, updates the user's (organizer's) rating, and records an "Review Submitted" event in an internal event log.
    2.  **Decoupled Notification Processing:** Significant system events (e.g., event registration, cancellation, review submissions) are first recorded as `EventLog` entries. Dedicated, scheduled backend processors then consume these `EventLog` entries:
        * One processor generates and saves in-app notifications into the `notifications` table for the web application to fetch via long polling.
        * The existing Telegram notification system (leveraging `ITelegramOutboxMessagePublisher`) will consume relevant `EventLog` entries to dispatch messages via the Telegram API.
        * **Each processor independently tracks its processing progress, without modifying the `EventLog` entry itself.**
    3.  **In-App Notification Management:** The frontend fetches unread in-app notifications via API (using long polling and cursor-based pagination), displays them, and can mark them as read via API calls.
    4.  **Review Moderation (by User-Organizer):** A user who is an event organizer can hide or unhide reviews for their events via API, controlling what is publicly visible.
-   **System architecture:** The system utilizes a layered Spring Boot backend with RESTful APIs, Spring Data JDBC for persistence, and a new decoupled event logging and processing mechanism for notifications, integrating with the existing Telegram API infrastructure.

## 2. Development Principles and Best Practices

We commit to adhering to the following principles and best practices:
-   **RESTful API Design:** Proper use of HTTP methods, status codes, and resources for intuitive and effective interaction.
-   **Code Readability:** Writing clean, concise, and well-formatted code for better understanding and maintainability.
-   **SOLID Principles:** Adhering to SOLID principles (Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion) to create a flexible and maintainable architecture.
-   **High Cohesion and Low Coupling:** Maintaining high cohesion within components and low coupling between them.
-   **Single Responsibility Principle (SRP):** Ensuring each class/module has one specific responsibility.
-   **Keep it Simple (KISS):** Favoring simple solutions over complex ones.
-   **Don't Repeat Yourself (DRY)::** Avoiding code duplication by extracting reusable components.
-   **You Ain't Gonna Need It (YAGNI):** Avoiding implementing unnecessary features.
-   **Database Design:** Following good database design principles, normalizing tables to reduce redundancy and improve data integrity.
-   **Transaction Management:** Properly managing database transactions for data consistency.
-   **Spring Data JDBC Usage:** Effectively utilizing Spring Data JDBC features.
-   **Use DTOs:** Employing Data Transfer Objects to manage data flow between layers.
-   **Response Objects:** Using dedicated DTOs for API responses.
-   **Testing is Crucial:** Writing comprehensive unit and integration tests to ensure code quality and reliability.
-   **Idempotency:** Considering idempotency for critical operations.

## 3. Feature Specification

### 3.1 User Rating and Review System

-   **User story and requirements:** As an event participant, I want to see the average rating of a user who is an organizer to make informed decisions about attending their events. The rating is calculated as the sum of all star ratings divided by the total number of *all* reviews received by that user (regardless of their visibility).
-   **Detailed implementation steps:**
    1.  The `reviews` table will store `star_rating` and `is_hidden`.
    2.  A new `user_ratings` table will be introduced to store the cached average rating for each user.
    3.  `ReviewService` will include a method to calculate the average rating based on *all* reviews for a given user. This method will be invoked *only* after new reviews are submitted or existing reviews are deleted.
    4.  `UserRatingService` (NEW) will be responsible for updating the corresponding entry in the `user_ratings` table with each recalculation.
    5.  `ReviewController` will expose the following API endpoints:
        * `GET /api/users/{userId}/rating` to retrieve the cached average rating.
        * `GET /api/users/{userId}/reviews` (with pagination parameters `page`, `size`) to retrieve a list of reviews for a user.
-   **Error handling and edge cases:** If a user has no reviews, the system should return `0.0`.

### 3.2 Event Review System

-   **User story and requirements:** As an event participant, I want to leave a review and star rating for an event I have attended. Each review should include the date, time, star rating, and a text comment. I should be directed to the review page via a link in a Telegram notification after the event concludes. Only authenticated participants of a finished event should be able to submit a review. Reviews cannot be modified after submission in the current version, but they can be hidden/unhidden.
-   **Detailed implementation steps:**
    1.  A new `Review` entity will be defined and managed by `ReviewRepository`.
    2.  `ReviewController` will provide a `POST /api/reviews` endpoint to accept review data.
    3.  `ReviewService.submitReview()` will:
        * Validate that the event exists, is `FINISHED`, and the authenticated user was a participant (using existing `EventParticipantRepository.existsByEventIdAndUserId`).
        * Save the `Review` entity, including `participant_user_id`, `organizer_user_id` (the ID of the user who organized the event, obtained from `Event.getAuthorId()`), `star_rating`, `text_comment`, and `review_date_time`.
        * **Crucially, it will then create an `EventLog` entry of type `REVIEW_SUBMITTED` for later processing by notification managers.**
        * It will recalculate and update the user's (organizer's) entry in the `user_ratings` table (as a new review is added) via `UserRatingService`.
    4.  The event completion logic in `EventService.finishEvent()` will be enhanced to create an `EventLog` entry (e.g., `EVENT_FINISHED_FOR_REVIEW_LINK`) which the **Telegram-specific notification processor** will consume to send the review link via `ITelegramOutboxMessagePublisher`.
-   **Error handling and edge cases:** Handled with appropriate HTTP status codes (400, 403, 404, 409) for invalid input, unauthorized access, or duplicate submissions. All operations within `submitReview` will be transactional.

### 3.3 In-App Notifications (Backend API Focus)

-   **User story and requirements:** As a user, I want to receive notifications within the web application about important events (e.g., event registration, cancellation, reviews received). I want to see which notifications are unread and be able to mark them as read. The frontend will use long polling for efficient retrieval of these notifications.
-   **Detailed implementation steps:**
    1.  A new `Notification` entity will store in-app notifications, managed by `NotificationRepository`.
    2.  `NotificationService` will provide core logic for managing notifications: `getNotifications` (with optional read/unread filter), `markNotificationAsRead`, and `markAllNotificationsAsRead`.
    3.  `NotificationController` will expose REST endpoints:
        * `GET /api/notifications` (fetches notifications). **This endpoint will support cursor-based pagination using `lastCreatedAt` (timestamp) or `lastId` (UUID) and `limit` parameters for efficient long polling.** This allows fetching only new notifications since the last retrieval.
        * `POST /api/notifications/{id}/mark-as-read` (marks a single notification).
        * `POST /api/notifications/mark-all-as-read` (marks all unread notifications).
    4.  **Automated Notification Generation (Backend Internal Process):**
        * Instead of direct notification creation via `IEventNotificationService` (which is for Telegram), significant system events will now create `EventLog` entries.
        * A new **`InAppNotificationProcessor`** (a scheduled component) will periodically:
            * Retrieve its `last_processed_event_log_id` from the `notification_processor_progress` table.
            * Read unprocessed `EventLog` entries relevant to in-app notifications with IDs greater than the last processed one.
            * Based on the `event_type` and `payload` of the `EventLog`, it will then generate and save corresponding `Notification` records in the `notifications` table, marking them `unread`.
            * After successfully processing a batch of events, it will update its progress (new `last_processed_event_log_id` and `last_processed_at`) in the `notification_processor_progress` table.
        * Specific `EventLog` types to be generated: `EVENT_REGISTERED` (from `EventParticipationService.registerForEvent`), `EVENT_CANCELED` (from `EventManager.cancelEvent`), `REVIEW_SUBMITTED` (from `ReviewService.submitReview`).
-   **Error handling and edge cases:** Standard API error handling for unauthorized access or invalid notification IDs. The `InAppNotificationProcessor` will include robust logging and retry mechanisms for processing failures.

### 3.4 Review Moderation (by User-Organizer)

-   **User story and requirements:** As a user who is an event organizer, I want the ability to hide or unhide any reviews left on my events, controlling what is publicly visible. Hiding/unhiding a review should not affect its inclusion in the user's rating calculation, as the rating is based on all submitted reviews.
-   **Detailed implementation steps:**
    1.  The `reviews` table will gain an `is_hidden` boolean column.
    2.  `ReviewService` will implement `hideReview()` and `unhideReview()` methods. These methods will update the `is_hidden` flag for the specified review after validating that the authenticated user is the organizer of the event (by comparing `principal.userId()` with `review.organizer_user_id`).
    3.  `ReviewController` will provide `PATCH /api/reviews/{id}/hide` and `PATCH /api/reviews/{id}/unhide` endpoints.
    4.  **Important:** Changing the `is_hidden` status of a review will *not* trigger a recalculation of the user's average rating, as the rating calculation considers all reviews regardless of their visibility.
-   **Error handling and edge cases:** `404 Not Found` for non-existent reviews, `403 Forbidden` if the user is not the event organizer.

## 4. Database Schema

### 4.1 Tables

**`reviews` Table**

-   **Purpose:** Stores event reviews.
-   **Fields:**
    * `id` (`UUID`, Primary Key, `NOT NULL`)
    * `event_id` (`UUID`, Foreign Key to `events.id`, `NOT NULL`)
    * `participant_user_id` (`UUID`, Foreign Key to `users.id`, `NOT NULL`)
    * `organizer_user_id` (`UUID`, Foreign Key to `users.id`, `NOT NULL`)
    * `star_rating` (`INTEGER`, `NOT NULL`, CHECK (`star_rating` >= 1 AND `star_rating` <= 5))
    * `text_comment` (`TEXT`, `NULLABLE`)
    * `review_date_time` (`TIMESTAMP`, `NOT NULL`)
    * `is_hidden` (`BOOLEAN`, `NOT NULL`, DEFAULT `FALSE`)
-   **Relationships:** FKs to `events` and `users` tables.
-   **Indexes:** `idx_reviews_event_participant` (`event_id`, `participant_user_id`, UNIQUE), `idx_reviews_organizer` (`organizer_user_id`), `idx_reviews_date_time` (`review_date_time`).

**`user_ratings` Table (NEW, renamed)**

-   **Purpose:** Stores the cached average rating for each user (who can be an organizer).
-   **Fields:**
    * `user_id` (`UUID`, Primary Key, Foreign Key to `users.id`, `NOT NULL`)
    * `average_rating` (`NUMERIC(3, 2)`, `NOT NULL`, DEFAULT `0.00`)
    * `total_reviews_count` (`INTEGER`, `NOT NULL`, DEFAULT `0`) - Total number of reviews (hidden and visible).
    * `last_calculated_at` (`TIMESTAMP`, `NOT NULL`)
-   **Relationships:** FK to `users` table.
-   **Indexes:** No additional indexes needed as `user_id` is the Primary Key.

**`notifications` Table**

-   **Purpose:** Stores in-app notifications for users.
-   **Fields:**
    * `id` (`UUID`, Primary Key, `NOT NULL`)
    * `user_id` (`UUID`, Foreign Key to `users.id`, `NOT NULL`)
    * `type` (`VARCHAR(50)`, `NOT NULL`) - E.g., 'REGISTRATION', 'CANCELLATION', 'REVIEW_RECEIVED'.
    * `message` (`TEXT`, `NOT NULL`)
    * `is_read` (`BOOLEAN`, `NOT NULL`, DEFAULT `FALSE`)
    * `created_at` (`TIMESTAMP`, `NOT NULL`)
    * `related_entity_id` (`UUID`, `NULLABLE`) - Optional ID of the related entity (e.g., event ID, review ID).
-   **Relationships:** FK to `users` table.
-   **Indexes:** `idx_notifications_user_read` (`user_id`, `is_read`), `idx_notifications_created_at` (`created_at`).

**`event_logs` Table (Updated)**

-   **Purpose:** Serves as a central log for system events that trigger notifications, enabling decoupled processing by different notification channels.
-   **Fields:**
    * `id` (`UUID`, Primary Key, `NOT NULL`)
    * `event_type` (`VARCHAR(50)`, `NOT NULL`) - Type of event (e.g., 'EVENT_REGISTERED', 'EVENT_CANCELED', 'REVIEW_SUBMITTED', 'EVENT_FINISHED_FOR_REVIEW_LINK').
    * `payload` (`JSONB`, `NULLABLE`) - JSON document containing event-specific details (e.g., `{"userId": "...", "eventId": "..."}`).
    * `created_at` (`TIMESTAMP`, `NOT NULL`) - When the event log was created.
-   **Relationships:** None direct, but conceptually links to other entities via `payload` data.
-   **Indexes:** `idx_event_logs_created_at` (`created_at`).

**`notification_processor_progress` Table (NEW)**

-   **Purpose:** Stores the independent processing progress for each notification processor.
-   **Fields:**
    * `id` (`UUID`, Primary Key, `NOT NULL`)
    * `processor_name` (`VARCHAR(100)`, `NOT NULL`, UNIQUE) - Unique name of the processor (e.g., 'IN_APP_NOTIFIER', 'TELEGRAM_NOTIFIER').
    * `last_processed_event_log_id` (`UUID`, `NULLABLE`) - ID of the last `event_log` entry successfully processed by this processor.
    * `last_processed_at` (`TIMESTAMP`, `NOT NULL`) - Timestamp of the last successful processing.
-   **Relationships:** No direct FK to `event_logs.id`, as `event_logs` are decoupled from processors' state. The relationship is logical.
-   **Indexes:** `idx_npp_processor_name` (`processor_name`, UNIQUE).

## 5. Server Actions

### 5.1 Database Actions

-   **Action: Insert Event Log Entry**
    * **Description:** Records a new system event in the `event_logs` table for asynchronous processing.
    * **Input parameters:** `EventType type`, `JsonNode payload`.
    * **Return values:** Saved `EventLog` entity.
    * **ORM operations:** `eventLogRepository.save()`.
-   **Action: Update Notification Processor Progress**
    * **Description:** Updates an entry in the `notification_processor_progress` table for a specific processor, indicating the last processed event log ID.
    * **Input parameters:** `String processorName`, `UUID lastProcessedEventLogId`.
    * **Return values:** `void`.
    * **ORM operations:** Update `last_processed_event_log_id` and `last_processed_at` for the entry with `processor_name`.
-   **Action: Fetch Unprocessed Event Logs for a Processor**
    * **Description:** Retrieves `event_log` entries that have not yet been processed by a specific notification channel, based on its last known progress.
    * **Input parameters:** `String processorName`, `int limit`.
    * **Return values:** `List<EventLog>`.
    * **Procedure:**
        1.  Retrieve `last_processed_event_log_id` for `processorName` from `notification_processor_progress`.
        2.  Execute a query to `event_logs` to retrieve entries with `created_at` greater than the `created_at` of `last_processed_event_log_id` (or `last_processed_at` from progress, for reliable ordering with UUIDs).
    * **ORM operations:** `notificationProcessorProgressRepository.findByProcessorName()`, `eventLogRepository.findByCreatedAtGreaterThanOrderByCreatedAtAsc()`.

### 5.2 Other Actions

-   **Telegram Integration (Decoupled Processing):**
    * **Component:** `TelegramNotificationProcessor` (NEW scheduled component). This processor will be responsible for consuming `EventLog` entries and using the *existing* `ITelegramOutboxMessagePublisher` to send messages.
    * **Procedure:** Periodically (e.g., every minute):
        1.  Retrieves its latest processing progress from `notification_processor_progress` (using `processor_name = "TELEGRAM_NOTIFIER"`).
        2.  Fetches a batch of `event_log` entries created after this progress, where `event_type` is relevant for Telegram notifications (`EVENT_FINISHED_FOR_REVIEW_LINK`, `EVENT_CANCELED`, `EVENT_REGISTERED`).
        3.  For each entry, it parses the `payload`, constructs the appropriate Telegram message (leveraging existing `NotificationType` enum and `EventNotificationService`'s message formatting if applicable, or new logic), and publishes it via `ITelegramOutboxMessagePublisher`.
        4.  After successfully processing the batch, it updates its progress (new `last_processed_event_log_id` and `last_processed_at`) in `notification_processor_progress`.
-   **Scheduled Event Log Processors:**
    * **Component:** `InAppNotificationProcessor`.
    * **Procedure:** A `@Scheduled` method (e.g., `fixedDelay`) periodically:
        1.  Retrieves its latest processing progress from `notification_processor_progress` (using `processor_name = "IN_APP_NOTIFIER"`).
        2.  Fetches `event_log` entries created after this progress.
        3.  For each entry, it determines the corresponding `NotificationType` and message content from the `event_type` and `payload`, creates and saves a `Notification` record via `NotificationService.createNotification()`.
        4.  After successfully processing the batch, it updates its progress (new `last_processed_event_log_id` and `last_processed_at`) in `notification_processor_progress`.

## 6. Component Architecture

### 6.1 Server Components

-   **`EventLog`** (`me.geohod.geohodbackend.domain.eventlog`)
    * JPA entity representing a system event for notification processing.
-   **`EventLogRepository`** (`me.geohod.geohodbackend.eventlog.repository`)
    * Spring Data JDBC interface for `EventLog` entity.
-   **`EventLogService`** (`me.geohod.geohodbackend.eventlog.service`)
    * Service to create `EventLog` entries and retrieve unprocessed logs.
    * Methods: `createLogEntry(EventType type, JsonNode payload)`, `getUnprocessedLogs(String processorName, int limit)`.
-   **`NotificationProcessorProgress`** (`me.geohod.geohodbackend.notification.progress`)
    * New JPA entity for tracking each processor's progress.
-   **`NotificationProcessorProgressRepository`** (`me.geohod.geohodbackend.notification.progress.repository`)
    * Spring Data JDBC interface for `NotificationProcessorProgress` entity.
-   **`NotificationProcessorProgressService`** (`me.geohod.geohodbackend.notification.progress.service`)
    * Service for managing processor progress.
    * Methods: `getProcessorProgress(String processorName)`, `updateProcessorProgress(String processorName, UUID lastProcessedEventLogId)`.
-   **`ReviewService` (Updated)**
    * Will call `EventLogService.createLogEntry(EventType.REVIEW_SUBMITTED, ...)` after review submission.
    * Will update the entry in the `user_ratings` table (by calling `UserRatingService`) *only when a review is submitted or deleted*.
-   **`UserRatingService` (NEW, renamed)**
    * Service responsible for calculating and updating entries in the `user_ratings` table.
-   **`EventService` (Updated)**
    * Will call `EventLogService.createLogEntry(EventType.EVENT_FINISHED_FOR_REVIEW_LINK, ...)` when an event is marked as finished (within `finishEvent` method, if `sendPollLink` is relevant, it should indicate the review link trigger).
-   **`EventParticipationService` (Updated)**
    * Will call `EventLogService.createLogEntry(EventType.EVENT_REGISTERED, ...)` after user registration.
    * Will call `EventLogService.createLogEntry(EventType.EVENT_CANCELED, ...)` (or a new `EVENT_UNREGISTERED` type if distinguished) when a participant unregisters.
-   **`EventManager` (Updated)**
    * `cancelEvent` method will call `EventLogService.createLogEntry(EventType.EVENT_CANCELED, ...)` after event cancellation. (Currently, `EventManager` calls `eventNotificationService.notifyParticipantsEventCancelled(eventId);`, which needs to be replaced with EventLog creation).
-   **`InAppNotificationProcessor`** (`@Component`, `me.geohod.geohodbackend.notification.processor`)
    * A scheduled component responsible for consuming `EventLog` entries and creating `Notification` records.
    * Uses `EventLogService` to fetch logs and `NotificationProcessorProgressService` to manage its progress.
-   **`TelegramNotificationProcessor` (NEW)** (`me.geohod.geohodbackend.telegram.processor`)
    * This will be a *new* scheduled component.
    * It consumes `EventLog` entries relevant to Telegram, constructs messages, and publishes them using the *existing* `ITelegramOutboxMessagePublisher`.
    * This new processor will replace the direct `notificationService.notify...` calls in `EventService`, `EventParticipationService`, and `EventManager` for *Telegram-specific* notifications.
    * The existing `EventNotificationService` will be refactored or its methods used *internally* by `TelegramNotificationProcessor` for message formatting, but direct calls for *sending* will be removed from business logic.
-   **`NotificationController`**, **`NotificationService`**, **`NotificationRepository`**: Remain focused on serving the in-app notification API and managing the `notifications` table.

## 7. Data Flow

-   **Event Triggering:**
    1.  User performs an action (e.g., registers for event, submits review).
    2.  Corresponding service (e.g., `EventService`, `ReviewService`, `EventParticipationService`, `EventManager`) receives the request.
    3.  After successful processing, the service creates a new `EventLog` entry in the `event_logs` table via `EventLogService`.
-   **Asynchronous Notification Generation:**
    1.  `InAppNotificationProcessor` (scheduled) periodically:
        * Retrieves its `last_processed_event_log_id` from `notification_processor_progress`.
        * Queries `event_logs` for entries with `created_at` later than the last processed one.
        * For each such entry, it constructs an in-app `Notification` message and saves it to the `notifications` table via `NotificationService`.
        * It then updates its progress (new `last_processed_event_log_id` and `last_processed_at`) in `notification_processor_progress`.
    2.  Concurrently, `TelegramNotificationProcessor` (scheduled) periodically:
        * Retrieves its `last_processed_event_log_id` from `notification_processor_progress`.
        * Queries `event_logs` for entries with `created_at` later than the last processed one.
        * For each such entry, it constructs a Telegram message (potentially leveraging message formatting logic from the existing `EventNotificationService`) and dispatches it via `ITelegramOutboxMessagePublisher`.
        * It then updates its progress (new `last_processed_event_log_id` and `last_processed_at`) in `notification_processor_progress`.
-   **Frontend Consumption:**
    1.  Frontend uses long polling to continuously `GET /api/notifications` with `lastCreatedAt` (or `lastId`) and `limit` parameters.
    2.  Backend `NotificationController` and `NotificationService` retrieve relevant `Notification` records from the `notifications` table based on the cursor.
    3.  Frontend displays new notifications.
    4.  User interaction triggers `POST /api/notifications/{id}/mark-as-read` or `POST /api/notifications/mark-all-as-read` to update `is_read` status.

## 8. Testing

-   **Unit tests with JUnit/Mockito:**
    * **`EventLogServiceTest`**: Test `createLogEntry()`, `getUnprocessedLogs()`.
    * **`NotificationProcessorProgressServiceTest`**: Test `getProcessorProgress()`, `updateProcessorProgress()`.
    * **`InAppNotificationProcessorTest`**: Test the scheduled processing logic. Mock `EventLogService`, `NotificationService`, `NotificationProcessorProgressService`. Verify `Notification` creation and progress updates.
    * **`ReviewServiceTest` (Updated)**: Verify `EventLogService.createLogEntry(REVIEW_SUBMITTED, ...)` is called. Verify `user_ratings` update via `UserRatingService` only on review submission/deletion. Verify `is_hidden` changes do not trigger recalculation.
    * **`UserRatingServiceTest` (NEW)**: Test calculation and update logic for user ratings.
    * **`EventServiceTest` (Updated)**: Verify `EventLogService.createLogEntry(EVENT_FINISHED_FOR_REVIEW_LINK, ...)` call for finished event.
    * **`EventParticipationServiceTest` (Updated)**: Verify `EventLogService.createLogEntry()` calls for registration and unregistration.
    * **`EventManagerTest` (Updated)**: Verify `EventLogService.createLogEntry(EVENT_CANCELED, ...)` call for event cancellation.
    * **`TelegramNotificationProcessorTest` (NEW)**: Test its scheduled logic, ensuring it correctly reads `EventLog` entries and interacts with `ITelegramOutboxMessagePublisher` and `NotificationProcessorProgressService`.
    * **`EventNotificationService` (Refactored/Adjusted)**: If its message formatting logic is reused, unit test that logic.
-   **Integration tests with `@SpringBootTest`:**
    * **End-to-end flow for event-driven notifications**:
        * Simulate an action (e.g., user registers for event).
        * Verify an `EventLog` entry is created in DB.
        * Allow `InAppNotificationProcessor` and `TelegramNotificationProcessor` to run (e.g., using `TestPropertyValues` to adjust scheduler, or manually triggering the processors' methods).
        * Verify an in-app `Notification` record is created.
        * Verify Telegram message is sent (mocking `ITelegramOutboxMessagePublisher`).
        * Verify each processor's progress is updated in `notification_processor_progress`.
    * **Concurrency for processors**: Ensure `EventLog` entries are processed exactly once per channel, even with concurrent access to `notification_processor_progress`.
    * **Failure scenarios**: Test how the system handles processor failures and retries for `EventLog` processing (considering the progress mechanism).
    * **Pagination for Notifications**: Test `GET /api/notifications` with `lastCreatedAt`/`lastId` and `limit` parameters to ensure correct cursor-based pagination.
-   **Data integrity tests**: Ensure `user_ratings` are always accurate.