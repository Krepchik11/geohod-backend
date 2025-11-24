# Active Context: Geohod Backend

## Current Focus (November 2025)

**Primary Focus: SQL Optimization and Raw JSON Properties (November 2025)**

Recent optimizations have been implemented including enhanced SQL query construction for better performance and proper JSON serialization for event log payloads using Jackson ObjectMapper.

**Key Implementation Details**:
- **SQL Query Optimization**: Enhanced `EventProjectionRepository` with dynamic WHERE clause building using StringBuilder and Map-based parameter handling
- **JSON Serialization**: Added Jackson ObjectMapper dependency and `toJson()` method for proper event log payload serialization
- **Query Performance**: Improved query structure with separated base SQL, WHERE clause, and ORDER BY for better readability and performance
- **Parameter Handling**: Optimized parameter binding using HashMap for named parameters instead of individual parameter binding
- **Code Quality**: Refactored string-based JSON creation to proper ObjectMapper serialization for better data integrity

### SQL Optimization and Raw JSON Properties (November 2025)

**Performance and Code Quality Enhancements**:
- **SQL Query Optimization**: Refactored `EventProjectionRepository.events()` method with dynamic WHERE clause construction
- **Parameter Handling**: Implemented Map-based parameter binding for better performance and maintainability
- **JSON Serialization**: Added Jackson ObjectMapper for proper event log payload serialization
- **Code Refactoring**: Replaced string-based JSON formatting with structured ObjectMapper usage
- **Query Structure**: Separated base SQL, WHERE clause, and ORDER BY for improved readability
- **Performance Benefits**: Enhanced query execution through optimized parameter binding and cleaner SQL structure

### Event State and Participant State Implementation (November 2025)

**Enhanced Event Management Capabilities**:
- **Event States**: Added support for tracking poll link sending and donation preferences (cash/transfer) at the event level
- **Participant States**: Implemented individual participant state tracking for poll link delivery and donation completion
- **State Management API**: New `UpdateParticipantStateRequest` with fields for `pollLinkSent`, `cashDonated`, `transferDonated`
- **Database Migration**: Liquibase changelog `db.changelog-2.4-add-event-action-states.xml` with boolean fields for all state tracking
- **Repository Updates**: Enhanced data access layer to support state-based operations
- **Service Integration**: Updated `EventParticipationService` with state management functionality
- **API Response Enhancement**: Extended `EventDetailsResponse` and `EventDetailedProjection` to include state information

### Previous Focus: Notification System Refactoring Completion (Completed November 2025)

The notification system refactoring has been successfully completed, introducing a **unified NotificationStrategy pattern** that significantly improves maintainability and reduces code duplication:

1. **Unified Strategy Interface**: Updated NotificationStrategy to support both In-App and Telegram channels with shared and channel-specific methods
2. **Strategy Registry Integration**: All processors now delegate to StrategyRegistry for consistent strategy selection
3. **Code Deduplication**: Eliminated hardcoded recipient determination and DTO creation logic across processors
4. **Enhanced Architecture**: Cleaner separation of concerns with strategy implementations handling business logic

## Recent Major Changes

### SQL Optimization and Raw JSON Properties (November 2025)

**Performance and Code Quality Enhancements**:
- **SQL Query Optimization**: Refactored `EventProjectionRepository.events()` method with dynamic WHERE clause construction
- **Parameter Handling**: Implemented Map-based parameter binding for better performance and maintainability
- **JSON Serialization**: Added Jackson ObjectMapper for proper event log payload serialization
- **Code Refactoring**: Replaced string-based JSON formatting with structured ObjectMapper usage
- **Query Structure**: Separated base SQL, WHERE clause, and ORDER BY for improved readability
- **Performance Benefits**: Enhanced query execution through optimized parameter binding and cleaner SQL structure

### Event State and Participant State Implementation (November 2025)

**State Management System Implementation**:
- **Database Schema Enhancement**: Added 6 new boolean fields across events and event_participants tables for comprehensive state tracking
- **Event-Level States**: `send_poll_link`, `donation_cash`, `donation_transfer` for managing event-level preferences
- **Participant-Level States**: `poll_link_sent`, `cash_donated`, `transfer_donated` for tracking individual participant progress
- **API Implementation**: New request DTO `UpdateParticipantStateRequest` for state updates
- **Service Layer Updates**: Enhanced `EventParticipationService` with state management capabilities
- **Repository Enhancements**: Updated data access layer to support state-based operations
- **Data Transfer Objects**: Enhanced `EventDetailsResponse` and `EventDetailedProjection` to include state information
- **Database Migration**: Liquibase changelog 2.4 with proper default values (false) for all new fields

### Event Sorting Implementation Completed (November 2025)

**Dynamic Sorting Support Implementation**:
- **Repository Enhancement**: Enhanced `EventProjectionRepository.events()` method with dynamic ORDER BY clause building
- **Sorting Fields**: Complete support for name, date, status, createdAt, updatedAt with proper database column mapping
- **Implementation**: Custom `buildOrderByClause()` method with switch statement mapping API fields to database columns
- **Default Behavior**: Smart fallback to `e.created_at DESC` when no sort specified or unknown field provided
- **Test Coverage**: Created comprehensive `EventProjectionRepositoryTest` with Testcontainers integration
- **Verification**: All 4 sorting tests passed successfully:
  - `shouldSortEventsByNameAsc()`
  - `shouldSortEventsByDateDesc()`
  - `shouldSortEventsByStatus()`
  - `shouldSortEventsByCreatedAtDefault()`

### Notification System Refactoring Completed (November 2025)

**Unified Strategy Pattern Implementation**:
*   **NotificationStrategy Interface Update**: Enhanced to support both channels with:
    - Shared methods: `getType()`, `isValid()`, `getRecipients()`
    - Telegram-specific: `createTelegramParams()`, `formatTelegramMessage()`
    - In-App-specific: `createInAppNotification()`
*   **Strategy Implementations Refactored**: All five strategies updated to implement unified interface:
    - `EventCancelledStrategy`, `EventCreatedStrategy`, `EventFinishedStrategy`
    - `ParticipantRegisteredStrategy`, `ParticipantUnregisteredStrategy`
*   **Processor Modernization**: 
    - `InAppNotificationProcessor`: Refactored to use StrategyRegistry and delegate logic
    - `TelegramNotificationProcessor`: Updated method names for consistency
*   **Code Quality Improvements**: Removed hardcoded recipient determination and DTO creation logic

**Previous Enhancements (October-November 2025)**:
*   **Template Engine**: Built sophisticated `TemplateEngine` supporting:
    - Variable interpolation: `{{variable}}`, `{{variable|fallback}}`, `{{variable:50}}`
    - Conditional blocks: `{#if condition}content{/if}`
    - Automatic fallback values for missing variables
*   **Telegram MarkdownV2 Formatter**: Complete implementation with proper escaping for special characters and URL preservation
*   **MessageTemplate Registry**: System for managing notification templates by ID and type
*   **Channel-Specific Formatting**: Separate formatting for Telegram (MarkdownV2) and in-app (plain text) notifications

### User Settings System Enhancement (Completed)

*   **Granular API**: Four dedicated PUT endpoints for individual settings updates
*   **New Fields**: `paymentGatewayUrl` and `showBecomeOrganizer` (both optional)
*   **Database Support**: Full migrations and model support implemented
*   **Business Logic**: Service layer handles creation of default settings and field updates

### API Evolution

*   **v2 Multi-participant Registration**: Support for registering 1-10 participants in single request
*   **v3 API Development**: Emerging API v3 with enhanced features (mentioned in branches)
*   **User Statistics**: Enhanced user details and stats endpoints with comprehensive metrics

## Recent Technical Improvements

*   **Monitoring**: Added Micrometer with Prometheus integration for metrics collection
*   **Testing**: Enhanced Testcontainers integration for PostgreSQL-based integration tests
*   **Performance**: Multiple performance optimization branches and database index improvements
*   **Deployment**: Optimized CI/CD with Podman migration and better environment tracking

## Key Learnings & Insights

*   The notification system has evolved from a simple enum-based approach to a sophisticated strategy and template-driven architecture
*   **Telegram message formatting is complex**: Requires careful handling of MarkdownV2 special characters and URL escaping
*   **Template systems require robust fallback handling** to ensure notifications always deliver meaningful content
*   **Russian-language templates** are now the primary interface for notifications
*   **Multi-participant registration** significantly improves user experience for group events
*   **User settings have evolved** to support monetization features (payment gateway) and organizer promotion capabilities

## Current Development Status

The system shows active development across multiple fronts:
- **Core notification functionality**: Template system and formatting completed
- **User interface evolution**: Settings and preferences fully implemented  
- **API modernization**: Ongoing development of v3 APIs
- **Operational excellence**: Enhanced monitoring, testing, and deployment capabilities
