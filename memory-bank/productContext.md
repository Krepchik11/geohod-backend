# Product Context: Geohod

## Problem Space

Organizing and finding local, community-driven events can be fragmented across various social media platforms or messaging apps. It's often difficult for organizers to manage participants and for attendees to track events and build a reputation within a community.

## Product Vision

Geohod aims to be a centralized platform for event management, specifically tailored for communities that are active on Telegram. It simplifies the process of creating, managing, and participating in events. By integrating social features like reviews and ratings, it helps build trust and a sense of community among users.

## Target Audience

The primary users are communities and individuals who use Telegram for communication and coordination. This could include hobby groups, local clubs, or any social circle that organizes regular or one-off events.

## User Experience Goals

*   **Seamless Integration**: Users should have a smooth experience authenticating and receiving notifications via Telegram.
*   **Effortless Event Management**: Organizers should find it easy to create events, manage participants, and finalize event details.
*   **Trust and Safety**: The review and rating system should help users make informed decisions about which events and organizers to engage with.
*   **Clear Communication**: The notification system should keep users informed about relevant event updates.

## Core Capabilities

*   **Event Management**: Create, update, cancel, and finish events with detailed projections and lifecycle tracking.
*   **Event Participation**: Register for or unregister from events; list and manage participants. Supports registering multiple participants (up to 3) in a single request via the `amountOfParticipants` field in the register endpoint, with a default of 1 for backward compatibility.
*   **Reviews and Ratings**: Submit and update reviews (one per user per event); aggregate user ratings based on review scores.
*   **Users**: Automatic user creation and updates via Telegram authentication; retrieve user details, aggregated statistics (ratings, event involvement), and settings.
*   **Notifications**: Reliable delivery of in-app and Telegram notifications using an outbox pattern for events like registrations and updates.
*   **Authentication**: Stateless validation of Telegram WebApp init data.
*   **API Versioning**: v1 for legacy compatibility; v2 as the primary interface with consistent `ApiResponse<T>` wrappers.
