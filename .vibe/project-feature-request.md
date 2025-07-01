# Request
Event Management System - User Settings for Default Donation and Participant Amounts

## Request Description
Implement a new feature in the event management backend system that allows users to set and manage default values for donation amounts and the maximum number of participants for events. These settings should be saved per user and made accessible via an API. This feature aims to streamline event creation by providing personalized default options.

## Target Audience
- [X] Any registered user who can organize events.
- [X] Each user will have access to their own settings, with no different levels of access or permissions based on roles for these specific settings.

## Desired Features
### User Settings Management
- [X] Users should be able to specify a default donation amount.
- [X] Users should be able to specify a default maximum number of participants.
- [X] These settings should be persistently stored for each user.
- [X] **Initial State:** Default values for `donation amount` and `maximum participants` should be `null`/empty until explicitly set by the user.

### API Endpoints
- [X] An API endpoint to retrieve a user's default settings using the `GET` HTTP method.
- [X] An API endpoint to update a user's default settings using the `PUT` HTTP method.
- [X] These settings are general user preferences applicable across all events the user creates.
- [X] **Payload Structure:** The request/response payload structure for these API calls should align with existing patterns in the project code.

## User experience requests
- [X] Interaction with these settings will primarily be through a web interface, with the backend providing the necessary APIs.
- [X] No specific validation (e.g., minimum/maximum values, data type checks) on the input values for donation amount or maximum participants is required for initial implementation.

## Other Notes
- [X] These default values will be requested by the frontend and used to pre-fill the event creation form. They will not automatically apply unless overridden.
- [X] There are no existing "settings" mechanisms in the backend to align with or leverage for this new feature.
- [X] Best practice concerns include: Clean, efficient, secure, scalable, high level of readability, and high level of maintainability.
- [X] **Data Storage:** A new dedicated `UserSettings` model/table should be implemented for storing these settings.
- [X] **Error Handling:** API error handling should be consistent with existing implementations in the project code.
- [X] **Future Extensibility:** No other user-specific settings are currently foreseen to be added in the future.
- [X] **Authentication and Authorization:** User identity will be established using the existing `SecurityContext` during HTTP requests.
- [X] **Concurrency:** Simultaneous updates by different users to their *own* settings are not considered a race condition as each user's settings are independent.
- [X] **Migration Strategy:** A database migration will be required to add the new `UserSettings` table.