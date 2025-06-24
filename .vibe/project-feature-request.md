# Desired Features

## User Reputation and Feedback

-   **User Rating System**
    * Implement a rating mechanism for event organizers based on participant reviews.
    * Calculate the average rating for organizers as the sum of all star ratings divided by the total number of *all* reviews received by that user, regardless of their visibility (hidden or unhidden).
-   **Event Review System**
    * Allow participants to leave reviews and ratings for events they have attended.
    * Each review should include the date, time, star rating (1-5), and an optional text comment.
    * Integrate a link in the Telegram notification sent upon event completion, directing participants to the review submission page.
    * Restrict review submission to authenticated users who were participants of the finished event. Reviews cannot be modified after submission.

## In-App Notifications (Backend API Focus)

-   **Notification Management API**
    * Implement backend APIs to manage user notifications within the web interface.
    * The API should support fetching notifications using long polling with cursor-based pagination (e.g., `lastCreatedAt` or `lastId` and `limit` parameters).
    * The API should support tracking the read/unread status of each notification.
    * Provide API endpoints for marking individual notifications as read and marking all unread notifications for a user as read.
-   **Automated Notification Generation (Backend Internal Process)**
    * Implement a decoupled event logging mechanism (`EventLog`) where significant system events (e.g., event registration, cancellation, review submissions) are first recorded as `EventLog` entries.
    * Dedicated, scheduled backend processors will consume these `EventLog` entries to generate and save in-app notifications.
    * Specific events triggering in-app notifications:
        * **Event Registration:** When a user successfully registers for an event, an unread notification must be generated and saved for that user.
        * **Event Cancellation:** When an event is canceled, unread notifications must be generated and saved for all registered participants.
        * **Organizer Review Received:** When a new review is left for an event, an unread notification must be generated and saved for the organizer of that event.

-   **Review Moderation (Organizer)**
    * Provide an API for event organizers to hide or unhide any reviews left on their events, controlling what is publicly visible.
    * **Important:** Hiding or unhiding a review must not affect its inclusion in the user's average rating calculation, as the rating is based on all submitted reviews.

## Other Notes

## Potential Technical Challenges & Important Decisions:

-   **Database Schema Changes:** New tables/columns will be required for storing user ratings, reviews, `EventLog` entries, notification processor progress, and in-app notifications.
-   **API Endpoints:** New RESTful API endpoints will be needed for submitting reviews (`POST /api/reviews`), fetching user ratings (`GET /api/users/{userId}/rating`), retrieving reviews for a user (`GET /api/users/{userId}/reviews`), managing in-app notifications (`GET /api/notifications`, `POST /api/notifications/{id}/mark-as-read`, `POST /api/notifications/mark-all-as-read`), and review moderation (`PATCH /api/reviews/{id}/hide`, `PATCH /api/reviews/{id}/unhide`).
-   **Telegram Integration:** Ensure seamless and asynchronous integration for sending review links and other event-related notifications via Telegram. This will be achieved by recording relevant events in `EventLog` and having a dedicated `TelegramNotificationProcessor` consume these logs to dispatch messages via the existing `ITelegramOutboxMessagePublisher`. Direct calls to Telegram notification services from core business logic will be replaced by `EventLog` creation.
-   **Notification Delivery Mechanism:**
    * The preference is for long polling for in-app notifications, where the frontend repeatedly requests new notifications from the backend API using cursor-based pagination.
    * Web Push Notifications are generally more complex to implement on the backend and client-side compared to long polling for this scope, and are not a priority.