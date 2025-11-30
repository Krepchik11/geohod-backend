# Product Context: Geohod

## Problem Space

Organizing and finding local, community-driven events can be fragmented across various social media platforms or messaging apps. It's often difficult for organizers to manage participants and for attendees to track events and build a reputation within a community. Users need sophisticated notification systems that work seamlessly across Telegram while maintaining community trust through transparent communication and feedback systems. The platform addresses these challenges with advanced state management and comprehensive user experience features.

## Product Vision

Geohod aims to be a centralized platform for event management, specifically tailored for communities that are active on Telegram. It simplifies the process of creating, managing, and participating in events while providing sophisticated communication capabilities. The platform has evolved to support group events, monetization features, advanced notification customization, and comprehensive state tracking for poll links and donations. **Recent expansion includes API v3 capabilities with enhanced user settings and detailed user statistics.**

## Target Audience

The primary users are communities and individuals who use Telegram for communication and coordination. This includes hobby groups, local clubs, professional meetups, and any social circle that organizes regular or one-off events. **Recent expansion**: Support for group registrations, monetization through payment gateway integration, organizer promotion features, and enhanced user profile management through API v3.

## User Experience Goals

*   **Seamless Integration**: Users should have a smooth experience authenticating and receiving notifications via Telegram with rich formatting and state tracking.
*   **Effortless Event Management**: Organizers should find it easy to create events, manage multiple participants, track poll link delivery, and monitor donation completion.
*   **Trust and Safety**: The review and rating system should help users make informed decisions about which events and organizers to engage with, with comprehensive user statistics.
*   **Clear Communication**: The notification system should keep users informed about relevant event updates with personalized, well-formatted messages using sophisticated templating.
*   **Group Event Support**: Make it easy to register multiple participants in single requests with proper capacity validation.
*   **Monetization Ready**: Support for payment gateway integration and organizer promotion features through enhanced user settings.
*   **Enhanced User Profiles**: **NEW**: Comprehensive user statistics including ratings distribution, event involvement counts, and detailed profile information.

## Core Capabilities (Updated November 2025)

*   **Event Management**: Create, update, cancel, and finish events with detailed projections and lifecycle tracking. **Enhanced**: State tracking for poll link sending preferences and donation acceptance (cash/transfer).
*   **Event Participation**: Register for or unregister from events; list and manage participants. **Enhanced**: Support for registering multiple participants (1-10) in a single request via `amountOfParticipants` field with full capacity validation. **NEW**: Event state and participant state tracking for poll link delivery and donation progress monitoring with comprehensive boolean state fields.
*   **Reviews and Ratings**: Submit and update reviews (one per user per event with database enforcement); aggregate user ratings based on review scores with comprehensive statistics.
*   **Users**: Automatic user creation and updates via Telegram authentication; retrieve user details, comprehensive statistics (ratings, event involvement, review distribution), and **personalized settings with enhanced API v3 features**.
*   **Notifications**: **Major Enhancement**: Sophisticated template-driven notifications with:
  - Strategy-based notification types and processing with unified interface
  - Variable interpolation and conditional content rendering
  - Channel-specific formatting (Telegram MarkdownV2, in-app plain text)
  - Russian-language templates with fallback handling
  - Outbox pattern for reliable delivery with processing progress tracking
*   **User Settings**: **Enhanced Granular Settings System**:
  - Payment gateway URL for monetization support
  - Show/hide organizer advertisement preference
  - Default event parameters (max participants, default donation amount)
  - API-level granular updates via dedicated endpoints
*   **User Statistics** (**API v3 NEW**):
  - Overall rating calculation with precise decimal precision
  - Review distribution by rating (1-5 scale) with guaranteed key presence
  - Event participation counts (organized events, participated events)
  - Enhanced user profile with avatar images and contact information
*   **State Management** (**NEW**):
  - Event-level states: poll link sending preferences, donation acceptance (cash/transfer)
  - Participant-level states: poll link delivery confirmation, donation completion tracking
  - Atomic state updates via dedicated API endpoints
  - Comprehensive state display in event details and projections
*   **Authentication**: Stateless validation of Telegram WebApp init data with custom security flow and method-level authorization.
*   **API Versioning**: v1 (completely removed), v2 as the primary interface with `ApiResponse<T>` wrappers; **Emerging v3** for enhanced features including granular user settings and comprehensive user statistics.

## User Experience Evolution

The platform has significantly evolved to support more sophisticated use cases:
- **Group Events**: Multiple participant registration reduces friction for family and friend group attendance with capacity validation
- **Rich Notifications**: Well-formatted, personalized messages in native Telegram style improve engagement through sophisticated template processing
- **Monetization**: Payment gateway integration opens new business models for event organizers with dedicated user settings
- **Preference Control**: Users can control their experience through granular notification and display preferences with dedicated API endpoints
- **Community Trust**: Enhanced review system with one-to-one constraints ensures fair and transparent feedback with comprehensive statistics
- **State Transparency**: **NEW**: Complete visibility into event and participant state for poll links and donations
- **Enhanced Profiles** (**API v3**): **NEW**: Detailed user statistics and profile information improve community interaction and trust
- **Performance** (**November 2025**): **NEW**: Optimized database queries and SQL construction improve response times and user experience

## API Evolution and Features

### API v2 (Current Primary)
- Consistent `ApiResponse<T>` wrapper for all endpoints
- Multi-participant registration support (1-10 participants)
- Enhanced event sorting with dynamic ORDER BY clause building
- Comprehensive user settings with granular API endpoints
- Event and participant state management
- Advanced notification system with strategy-based processing

### API v3 (Emerging Features)
- **User Settings Enhancement**:
  - `PUT /api/v2/user/settings/default-max-participants` - Granular max participants setting
  - `PUT /api/v2/user/settings/payment-gateway-url` - Payment gateway URL management
  - `PUT /api/v2/user/settings/show-become-organizer` - Organizer advertisement control
  - `PUT /api/v2/user/settings` - Comprehensive settings update with all fields
- **User Statistics**:
  - `GET /api/v2/users/{id}/stats` - Comprehensive user statistics with ratings, counts, and distribution
  - `GET /api/v2/users/{id}` - Enhanced user profile with avatar and contact information
- **Enhanced Validation**: Comprehensive DTO validation with @Min, @Max, @NotNull annotations
- **Backward Compatibility**: Optional request bodies with service-level defaults

### Security Evolution
- **v1 Removal**: Complete elimination of legacy API endpoints
- **Method-Level Security**: @PreAuthorize annotations with EventSecurity service
- **Centralized Authorization**: Event-specific authorization logic in dedicated service
- **Declarative Security**: Modern security approach preparing for future API evolution

## Success Metrics

The platform's success is measured through:
- **User Engagement**: High notification delivery rates and template processing success
- **Event Management**: Efficient multi-participant registration and state tracking
- **Community Trust**: Comprehensive user statistics and review distribution
- **Performance**: Optimized database queries and fast response times
- **Developer Experience**: Clean API design with comprehensive documentation and testing
- **Reliability**: Robust notification processing with outbox pattern and retry mechanisms
