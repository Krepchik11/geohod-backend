# Product Context: Geohod

## Problem Space

Organizing and finding local, community-driven events can be fragmented across various social media platforms or messaging apps. It's often difficult for organizers to manage participants and for attendees to track events and build a reputation within a community. Users need sophisticated notification systems that work seamlessly across Telegram while maintaining community trust through transparent communication and feedback systems.

## Product Vision

Geohod aims to be a centralized platform for event management, specifically tailored for communities that are active on Telegram. It simplifies the process of creating, managing, and participating in events while providing sophisticated communication capabilities. The platform has evolved to support group events, monetization features, and advanced notification customization.

## Target Audience

The primary users are communities and individuals who use Telegram for communication and coordination. This includes hobby groups, local clubs, professional meetups, and any social circle that organizes regular or one-off events. **Recent expansion**: Support for group registrations and monetization through payment gateway integration.

## User Experience Goals

*   **Seamless Integration**: Users should have a smooth experience authenticating and receiving notifications via Telegram with rich formatting.
*   **Effortless Event Management**: Organizers should find it easy to create events, manage multiple participants, and finalize event details.
*   **Trust and Safety**: The review and rating system should help users make informed decisions about which events and organizers to engage with.
*   **Clear Communication**: The notification system should keep users informed about relevant event updates with personalized, well-formatted messages.
*   **Group Event Support**: Make it easy to register multiple participants in single requests.
*   **Monetization Ready**: Support for payment gateway integration and organizer promotion features.

## Core Capabilities (Updated November 2025)

*   **Event Management**: Create, update, cancel, and finish events with detailed projections and lifecycle tracking.
*   **Event Participation**: Register for or unregister from events; list and manage participants. **Enhanced**: Support for registering multiple participants (1-10) in a single request via `amountOfParticipants` field with full capacity validation.
*   **Reviews and Ratings**: Submit and update reviews (one per user per event with database enforcement); aggregate user ratings based on review scores.
*   **Users**: Automatic user creation and updates via Telegram authentication; retrieve user details, comprehensive statistics (ratings, event involvement, review distribution), and **personalized settings**.
*   **Notifications**: **Major Enhancement**: Sophisticated template-driven notifications with:
  - Strategy-based notification types and processing
  - Variable interpolation and conditional content
  - Channel-specific formatting (Telegram MarkdownV2, in-app plain text)
  - Russian-language templates with fallback handling
  - Outbox pattern for reliable delivery
*   **User Settings**: **New Granular Settings System**:
  - Payment gateway URL for monetization
  - Show/hide organizer advertisement preference
  - Default event parameters (max participants, donation amounts)
  - API-level granular updates via dedicated endpoints
*   **Authentication**: Stateless validation of Telegram WebApp init data with custom security flow.
*   **API Versioning**: v1 for legacy compatibility; v2 as the primary interface with `ApiResponse<T>` wrappers; **Emerging v3** for enhanced features.

## User Experience Evolution

The platform has significantly evolved to support more sophisticated use cases:
- **Group Events**: Multiple participant registration reduces friction for family and friend group attendance
- **Rich Notifications**: Well-formatted, personalized messages in native Telegram style improve engagement
- **Monetization**: Payment gateway integration opens new business models for event organizers
- **Preference Control**: Users can control their experience through granular notification and display preferences
- **Community Trust**: Enhanced review system with one-to-one constraints ensures fair and transparent feedback
