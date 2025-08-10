# Geohod Backend — Memory Bank Brief

This brief is the foundation for the Memory Bank. It captures the project scope, core objectives, and anchors all other memory-bank documents. Update this file manually if the product direction or scope changes materially.

## Purpose

Provide a robust, scalable Spring Boot backend for Geohod — a Telegram-integrated event management platform. The backend exposes RESTful APIs for events, participation, users, reviews, and notifications, with reliable delivery and clear API versioning.

## Scope

- Event lifecycle: create, update, cancel, finish.
- Participation: register/unregister, participant lists.
- Reviews and ratings: submit and compute user ratings.
- Notifications: in-app and Telegram via outbox pattern.
- Authentication: Telegram init data through custom Spring Security provider.
- API versioning: v1 (legacy) and v2 (current) with ApiResponse<T> wrapper.
- Operational tooling: Liquibase migrations, Actuator health, OpenAPI docs.

## Non-Goals

- Rich front-end UX (out of backend scope).
- Complex analytics beyond basic counts/ratings.
- Non-Telegram auth providers at this time.

## Success Criteria

- Reliable Telegram-authenticated access and notification delivery.
- Consistent v2 API contract with ApiResponse<T>.
- Database migrations applied automatically and safely (Liquibase).
- Health endpoints and OpenAPI available in dev profile.