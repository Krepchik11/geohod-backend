# Product: Geohod Backend

## Why this exists
Geohod provides community event management tightly integrated with Telegram for seamless auth and notifications. The backend exposes APIs to create/manage events, handle participation, capture reviews/ratings, and deliver notifications reliably.

## Users and stakeholders
- Event organizers: create/update/cancel/finish events, manage participants
- Participants: discover/join/leave events, review other users
- Platform operators: observe health/metrics, deploy and operate the system

## Core capabilities
- Event lifecycle: create, update, cancel, finish; read event details and projections
- Participation: register/unregister; list participants; organizer participant management
- Reviews: submit/update single review per user-event; compute user ratings
- Users: create/update on first Telegram login; retrieve user details
- Notifications: outbox-based reliable notifications, Telegram delivery
- Authentication: validate Telegram WebApp init data; stateless security
- API versioning: v1 (legacy) and v2 (primary) endpoints with consistent responses
- Documentation and observability: OpenAPI in dev, Actuator health

## Flows (high-level)
1) Authentication
   - Client sends Telegram init data in Authorization header
   - Filter extracts token → AuthenticationProvider verifies and extracts user → creates/updates user → sets SecurityContext
2) Event management
   - REST controllers accept DTOs → service orchestrates repositories and mappers → DB
   - Event changes may emit notifications via outbox
3) Participation
   - Register/unregister endpoints enforce rules (capacity/ownership) via service layer
   - Participant projections exposed via DTOs
4) Reviews and ratings
   - Upsert-style review submission (one review per user per event)
   - Ratings aggregated via dedicated repository/projection
5) Notifications (outbox)
   - Business ops enqueue outbox message
   - Async processor delivers via Telegram bot with retries and progress tracking

## Non-goals
- Rich front-end UX
- Non-Telegram auth providers
- Complex analytics beyond basic counts/ratings

## Success criteria
- Reliable Telegram-authenticated access
- High notification delivery success rate
- Consistent v2 API with ApiResponse<T>
- Automated Liquibase migrations and healthy Actuator endpoints