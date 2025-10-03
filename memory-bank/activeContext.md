# Active Context: Review System Enhancements

## Current Focus

The current focus is on enhancing the UserSettings API to support granular updates for user preferences, including payment gateway configuration and organizer visibility.

## Recent Changes

*   Implemented a database unique constraint to prevent duplicate reviews.
*   Updated the `submitReview` method in `ReviewServiceImpl` to implement upsert logic.
*   Added a new API endpoint `GET /api/v2/reviews/event/{eventId}/my-review` in `ReviewController`.
*   Implemented the `getUserReviewForEvent` method in `ReviewServiceImpl`.
*   The keyset pagination fix has been successfully implemented and tested.

*   **UserSettings API Enhancements**: Added `paymentGatewayUrl` and `showBecomeOrganizer` fields to UserSettings DTOs. Deprecated and ignored `defaultDonationAmount`. Updated the mapper. Removed the PATCH endpoint for UserSettings. Added three specific PUT endpoints: `/max-participants` (with `MaxParticipantsRequest`), `/payment-gateway-url` (with `PaymentGatewayUrlRequest`), and `/show-become-organizer` (with `ShowBecomeOrganizerRequest`). This establishes granular update capabilities for user settings.

## Next Steps

1.  Implement service layer and model support for the new UserSettings fields (`paymentGatewayUrl`, `showBecomeOrganizer`).
2.  Add database migrations to include the new fields in the UserSettings table.
3.  Review the newly updated Memory Bank files for accuracy and completeness.
4.  Begin work on any active development tasks, using the Memory Bank as a guide.

## Key Learnings & Insights

*   The project is a well-structured Spring Boot application with a clear separation of concerns.
*   The reliance on Telegram for authentication is a critical and unique feature.
*   The use of `spring-data-jdbc` over `spring-data-jpa` is a significant architectural decision, suggesting a preference for more direct SQL control.
*   The system includes advanced patterns like the Outbox pattern for reliable notifications.
*   The review system now enforces one-to-one user-to-event reviews and provides an API endpoint to retrieve a user's existing review for an event.
