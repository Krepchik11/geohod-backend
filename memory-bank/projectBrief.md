# Project Brief: Geohod Backend

## Core Objective

This project is the backend service for "Geohod," an application that facilitates event management and participation, integrated with Telegram for user authentication and notifications. It provides a RESTful API for managing events, user participation, reviews, ratings, and notifications with sophisticated templating and formatting capabilities.

## Key Features

*   **Event Management**: Create, update, cancel, and finish events.
*   **Event Participation**: Register for, unregister from, and manage participants in events. **NEW: Support for registering multiple participants in a single request** (amountOfParticipants: 1-10). **ENHANCED: Event state and participant state tracking** for poll links and donation management.
*   **Reviews and Ratings**: Users can submit reviews for others, and the system calculates user ratings with one-to-one user-to-event constraint.
*   **Notifications**: **Major Enhancement: New strategy-based notification system** with sophisticated templating engine supporting variables, conditionals, and channel-specific formatting (Telegram MarkdownV2, in-app text).
*   **Telegram Integration**: User authentication is handled via Telegram, and the system can send notifications through a Telegram bot with **advanced message formatting**.
*   **API Versioning**: `v1` for legacy endpoints, `v2` for improved endpoints with ApiResponse<T> wrapper, **and emerging v3** for enhanced features.
*   **User Settings**: Granular user preferences including payment gateway URL and organizer advertisement settings.

## Technical Stack

*   **Language**: Java 23 (recently updated)
*   **Framework**: Spring Boot 3.3.5
*   **Build Tool**: Gradle 8.x
*   **Database**: PostgreSQL
*   **Database Migrations**: Liquibase with extensive changelog history
*   **Authentication**: Custom, based on Telegram init data.
*   **Monitoring**: **Micrometer with Prometheus** integration for metrics
*   **Testing**: **Testcontainers** for integration testing with real PostgreSQL
*   **Notification System**: **Complete refactor to strategy-based with template engine** 
*   **Message Formatting**: **Telegram MarkdownV2 formatter with proper escaping**
*   **Deployment**: Docker, with GitHub Actions for CI/CD and Podman optimization
