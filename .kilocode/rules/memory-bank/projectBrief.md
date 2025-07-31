# Project Brief: Geohod Backend

## Core Objective

This project is the backend service for "Geohod," an application that facilitates event management and participation, seemingly integrated with Telegram for user authentication and notifications. It provides a RESTful API for managing events, user participation, reviews, ratings, and notifications.

## Key Features

*   **Event Management**: Create, update, cancel, and finish events.
*   **Event Participation**: Register for, unregister from, and manage participants in events.
*   **Reviews and Ratings**: Users can submit reviews for others, and the system calculates user ratings.
*   **Notifications**: A system for delivering in-app or push notifications to users.
*   **Telegram Integration**: User authentication is handled via Telegram, and the system can send notifications through a Telegram bot.
*   **API Versioning**: The API is versioned, with `v1` for legacy endpoints and `v2` for newer, improved endpoints.

## Technical Stack

*   **Language**: Java 21+
*   **Framework**: Spring Boot 3.x
*   **Build Tool**: Gradle
*   **Database**: PostgreSQL
*   **Database Migrations**: Liquibase
*   **Authentication**: Custom, based on Telegram init data.
*   **Deployment**: Docker, with GitHub Actions for CI/CD.
