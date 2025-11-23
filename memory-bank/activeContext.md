# Active Context: Geohod Backend

## Current Focus (November 2025)

**Primary Focus: Event Sorting API Implementation (November 2025)**

The event sorting functionality has been successfully implemented, adding the ability to sort events by name, date, status, createdAt, and updatedAt fields with comprehensive test coverage and backward compatibility.

**Key Implementation Details**:
- **Repository Layer**: Enhanced `EventProjectionRepository` with dynamic ORDER BY clause building
- **Test Coverage**: Created `EventProjectionRepositoryTest` with Testcontainers integration
- **Sorting Support**: name→e.name, date→e.date, status→e.status, createdAt→e.created_at, updatedAt→e.updated_at
- **Default Behavior**: Falls back to created_at DESC when no sort specified
- **Backward Compatibility**: API works with and without sort parameters
- **Test Results**: All 4 sorting tests passed successfully
- **OpenAPI Documentation**: Added comprehensive sorting documentation for frontend developers with examples and field descriptions

**Previous Focus: Notification System Refactoring Completion (Completed November 2025)**

The notification system refactoring has been successfully completed, introducing a **unified NotificationStrategy pattern** that significantly improves maintainability and reduces code duplication:

1. **Unified Strategy Interface**: Updated NotificationStrategy to support both In-App and Telegram channels with shared and channel-specific methods
2. **Strategy Registry Integration**: All processors now delegate to StrategyRegistry for consistent strategy selection
3. **Code Deduplication**: Eliminated hardcoded recipient determination and DTO creation logic across processors
4. **Enhanced Architecture**: Cleaner separation of concerns with strategy implementations handling business logic

## Recent Major Changes

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
