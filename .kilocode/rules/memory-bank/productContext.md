# Product Context: Geohod Backend

## Why This Project Exists

Geohod is an event management platform that facilitates organizing and participating in events with a focus on community engagement. The backend service exists to provide a robust, scalable API for managing events, users, reviews, and notifications. The project aims to solve the problem of coordinating events in a distributed community by providing a centralized platform with Telegram integration for seamless user authentication and communication.

## Problems It Solves

1. **Event Discovery and Management**: Users need a way to discover, create, and manage events efficiently.
2. **User Authentication**: Traditional authentication methods can be cumbersome; Telegram integration provides a seamless authentication experience.
3. **Event Participation**: Managing event participation (registration, unregistration) requires a reliable system.
4. **Community Trust**: Reviews and ratings help build trust among community members.
5. **Communication**: Notifications are essential for keeping participants informed about event updates.
6. **API Evolution**: The system supports both legacy (v1) and modern (v2) API endpoints to ensure backward compatibility while introducing new features.

## How It Should Work

### Core Functionality

1. **Event Lifecycle Management**
   - Create events with details like title, description, location, date/time
   - Update event information as needed
   - Cancel events when necessary
   - Mark events as finished when completed

2. **User Management**
   - Authentication via Telegram init data
   - User profiles with ratings based on reviews
   - User discovery through Telegram integration

3. **Participation Management**
   - Register for events
   - Unregister from events
   - View event participants
   - Manage participant lists (for event organizers)

4. **Review System**
   - Submit reviews for other users
   - Calculate user ratings based on reviews
   - Display review history

5. **Notification System**
   - Send notifications for event updates
   - Deliver notifications via Telegram bot
   - Track notification delivery status

### Technical Workflow

1. **Authentication Flow**
   - User authenticates via Telegram
   - Backend validates Telegram init data
   - Creates or updates user profile
   - Returns authentication token

2. **Event Management Flow**
   - Event creator submits event details
   - Backend validates and stores event
   - System sends notifications to interested users
   - Participants can register/unregister
   - Event organizer can update/cancel/finish event

3. **Notification Flow**
   - System generates notifications for events
   - Notifications are queued for delivery
   - Telegram bot delivers notifications to users
   - System tracks delivery status

## User Experience Goals

1. **Seamless Authentication**: Users should be able to authenticate with minimal friction using their Telegram accounts.
2. **Intuitive Event Management**: Creating and managing events should be straightforward with clear feedback.
3. **Real-time Notifications**: Users should receive timely notifications about events they're interested in.
4. **Trust Building**: The review and rating system should help users make informed decisions about participating in events.
5. **Responsive Performance**: The API should respond quickly even under load.
6. **Consistent API Design**: The v2 API should provide a consistent, predictable interface for frontend developers.
7. **Reliable Communication**: Telegram notifications should be delivered reliably with proper error handling.

## Success Metrics

1. **User Engagement**: Number of active users, events created, and participations.
2. **System Reliability**: Uptime, error rates, and notification delivery success rates.
3. **API Usage**: Adoption of v2 API endpoints over v1 legacy endpoints.
4. **User Satisfaction**: Review ratings and feedback on the event experience.
5. **Performance**: API response times and system throughput under load.