# Project Brief: Geohod Backend

## Core Objective

This project is the backend service for "Geohod," an application that facilitates event management and participation, integrated with Telegram for user authentication and notifications. It provides a RESTful API for managing events, user participation, reviews, ratings, and notifications with sophisticated templating and formatting capabilities. The system has evolved to include comprehensive state management, API v3 enhancements, and advanced notification strategies.

## Key Features

*   **Event Management**: Create, update, cancel, and finish events with enhanced state tracking.
*   **Event Participation**: Register for, unregister from, and manage participants in events. **Enhanced**: Support for registering multiple participants in a single request** (amountOfParticipants: 1-10). **ENHANCED**: Event state and participant state tracking** for poll links and donation management with boolean fields for comprehensive state control.
*   **Reviews and Ratings**: Users can submit reviews for others, and the system calculates user ratings with one-to-one user-to-event constraint enforced at database level.
*   **Notifications**: **Major Enhancement**: New strategy-based notification system** with sophisticated templating engine supporting variables, conditionals, and channel-specific formatting (Telegram MarkdownV2, in-app text). **Enhanced**: Unified strategy pattern with StrategyRegistry for centralized management.
*   **Telegram Integration**: User authentication is handled via Telegram, and the system can send notifications through a Telegram bot with **advanced message formatting and comprehensive MarkdownV2 escaping**.
*   **API Versioning**: `v1` (completely removed), `v2` for improved endpoints with ApiResponse<T> wrapper, **and emerging v3** for enhanced features including granular user settings and comprehensive user statistics.
*   **User Settings**: Granular user preferences including payment gateway URL, organizer advertisement settings, and default event parameters.
*   **State Management**: **NEW**: Comprehensive event and participant state tracking including poll link sending preferences and donation completion status (cash/transfer).
*   **User Statistics**: **API v3**: Enhanced user profile retrieval and comprehensive statistics including ratings, event involvement, and review distribution.

## Technical Stack

*   **Language**: Java 23 (recently updated with latest features)
*   **Framework**: Spring Boot 3.3.5
*   **Build Tool**: Gradle 8.x
*   **Database**: PostgreSQL with 16+ Liquibase migrations
*   **Database Migrations**: Liquibase with extensive changelog history including performance indexes and state management
*   **Authentication**: Custom, based on Telegram init data with **method-level security using @PreAuthorize annotations**
*   **Monitoring**: **Micrometer with Prometheus** integration for metrics collection and observability
*   **Testing**: **Testcontainers** for integration testing with real PostgreSQL instances
*   **Notification System**: **Complete refactor to strategy-based with template engine and unified strategy interface** 
*   **Message Formatting**: **Telegram MarkdownV2 formatter with sophisticated escaping and URL preservation**
*   **Deployment**: Docker, with GitHub Actions for CI/CD and Podman optimization
*   **Security**: **Method-level security with EventSecurity service for centralized authorization**
*   **Performance**: **SQL optimization with dynamic query construction and Map-based parameter binding**

## Recent Major Enhancements (November 2025)

### API v3 Development
- **Enhanced User Settings**: Four dedicated PUT endpoints for granular settings updates (default-max-participants, payment-gateway-url, show-become-organizer)
- **User Statistics API**: Comprehensive user stats with ratings, event counts, review distribution by rating (1-5 scale)
- **User Details API**: Enhanced user profile retrieval with avatar images and contact information
- **API Documentation**: Complete API contract with DTO examples and response formats

### Security Modernization (Completed)
- **v1 API Removal**: Complete elimination of legacy v1 API endpoints for cleaner architecture
- **Method-Level Security**: @PreAuthorize annotations replacing manual AccessDeniedException checks
- **EventSecurity Service**: Centralized event authorization logic with isEventAuthor() method
- **Declarative Security**: Modern security approach preparing for API v3 evolution

### Database Evolution (16 Migrations)
- **State Management**: Event and participant state tracking for poll links and donations (6 new boolean fields)
- **User Settings Enhancement**: Payment gateway URL and organizer advertisement preferences
- **Performance Indexes**: Database optimization for user settings and query performance
- **Data Migration**: Proper handling of existing user data with backward compatibility

### Notification System Enhancement
- **Unified Strategy Pattern**: Complete refactor to single NotificationStrategy interface
- **Strategy Registry**: Centralized strategy management with dependency injection
- **Template Processing**: Enhanced template engine with variable interpolation and conditional blocks
- **Message Formatting**: Sophisticated Telegram MarkdownV2 formatting with proper escaping

### Performance Optimization
- **SQL Query Optimization**: Dynamic WHERE clause construction with StringBuilder
- **Parameter Binding**: Map-based parameter handling for better performance
- **JSON Serialization**: Jackson ObjectMapper for proper event log payload formatting
- **Event Sorting**: Dynamic ORDER BY clause building based on Pageable sort parameters

## Architecture Highlights

- **Layered Architecture**: Clean separation between API, Service, and Data Access layers
- **Strategy Pattern**: Sophisticated notification system with type-safe strategy selection
- **Template Engine**: Custom-built template processing supporting Russian language templates
- **State Management**: Boolean-based state tracking with comprehensive API support
- **Security**: Stateless authentication with method-level authorization
- **Monitoring**: Comprehensive observability with Prometheus metrics and health checks
- **Testing**: Real database integration testing with Testcontainers and PostgreSQL

## Development Workflow

- **Local Development**: Docker Compose for PostgreSQL, Gradle for application building
- **API Documentation**: OpenAPI/Swagger enabled in dev profile
- **Testing**: Comprehensive test suites with unit and integration tests
- **Deployment**: Container-based deployment with CI/CD pipeline
- **Migration**: Automated schema management via Liquibase changelogs
