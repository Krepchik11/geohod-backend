# Active Context: Geohod Backend

## Current Focus (November 2025)

**Primary Focus: Complete Notification System Refactor (Issue-84-optional-notifications)**

The project is currently undergoing a **major notification system refactor** with sophisticated templating and formatting capabilities. The new system introduces:

1. **Strategy-based notification architecture** with StrategyRegistry and dedicated strategy classes
2. **Advanced templating engine** supporting variables, conditionals, and fallback values
3. **Channel-specific formatting** with Telegram MarkdownV2 formatter and in-app text formatting
4. **Template registry system** with default Russian-language notification templates

## Recent Major Changes

### Notification System Overhaul (October-November 2025)

*   **New Strategy Pattern**: Implemented `StrategyNotificationType` enum replacing the deprecated `NotificationType`
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
