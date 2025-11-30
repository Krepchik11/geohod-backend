# Technical Context: Geohod Backend

## Core Technologies

*   **Java Version**: 23 (recently updated with latest features and performance improvements)
*   **Spring Boot**: 3.3.5 (with method-level security enhancements and latest stable features)
*   **Build Tool**: Gradle 8.x (with wrapper and comprehensive dependency management)
*   **Database**: PostgreSQL 17 (with 16+ migrations and performance optimizations)
*   **Web Server**: Embedded Tomcat (default for `spring-boot-starter-web`)
*   **Exception Handling**: **ENHANCED**: ResourceNotFoundException with proper HTTP 404 responses and centralized GlobalExceptionHandler

## Key Dependencies & Libraries (Updated November 2025)

### Core Dependencies
*   **Data Access**: `spring-boot-starter-data-jdbc`. Direct JDBC-based approach for database interactions with custom projections.
*   **Security**: `spring-boot-starter-security` with custom Telegram authentication flow and **method-level security support using @PreAuthorize annotations**.
*   **Web**: `spring-boot-starter-web` for building RESTful APIs with enhanced validation.
*   **Database Migrations**: `liquibase-core` for automated schema management (16+ changelog files with state management).
*   **Telegram Integration**: `telegrambots-spring-boot-starter:6.9.7.1` for Telegram Bot API integration with advanced formatting.

### Enhanced Monitoring & Metrics
*   **Monitoring**: `spring-boot-starter-actuator` with `micrometer-registry-prometheus` integration for comprehensive observability
*   **Metrics**: Comprehensive metrics collection for application performance and business metrics with health checks
*   **Health Checks**: Enhanced health endpoints including liveness and readiness states with multi-level monitoring

### Message Processing & Templating (Enhanced November 2025)
*   **Template Engine**: Custom-built template engine supporting:
  - Variable interpolation: `{{variable}}`, `{{variable|fallback}}`, `{{variable:50}}`
  - Conditional blocks: `{#if condition}content{/if}`
  - Automatic fallback values for missing variables
  - Russian language template support with proper encoding
*   **Message Formatting**: `TelegramMarkdownV2Formatter` with sophisticated character escaping and URL preservation
*   **Template Registry**: `MessageTemplateRegistry` for managing notification templates by type and ID with initialization
*   **Strategy Architecture**: **Enhanced**: Unified `NotificationStrategy` interface with StrategyRegistry for centralized management

### Development & Testing Tooling
*   **lombok**: Reduces boilerplate code for models and other classes with latest stable version
*   **mapstruct:1.5.5.Final**: Code generator for type-safe bean mappings with comprehensive configuration
*   **API Documentation**: `springdoc-openapi-starter-webmvc-ui:2.4.0` for OpenAPI 3.0 and Swagger UI with enhanced configuration
*   **Testing Suite**:
  *   `spring-boot-starter-test` (JUnit 5, Mockito) with enhanced testing support
  *   `spring-security-test`: For security configuration and method-level security testing
  *   **Testcontainers**: `testcontainers:junit-jupiter:1.19.7` and `testcontainers:postgresql:1.19.7` for integration testing with real PostgreSQL

### New Notification System Architecture (Enhanced November 2025)
*   **Strategy Pattern**: `StrategyRegistry` with `NotificationStrategy` implementations and unified interface
*   **Template System**: `MessageTemplate` records with variable, fallback, and type support with Russian language templates
*   **Channel Formatting**: Separate formatters for Telegram (MarkdownV2) and in-app (plain text) with enhanced escaping
*   **Processing**: Scheduled processors with configurable delays and progress tracking for reliability

### JSON Processing & Serialization (November 2025)
*   **Jackson ObjectMapper**: Integrated for proper event log payload serialization with error handling
*   **Structured Formatting**: Replaced string-based JSON with ObjectMapper for data integrity
*   **Error Handling**: Comprehensive exception handling for JSON processing failures

## Exception Handling and API Cleanup (November 2025)

### Complete v1 API Removal and Exception Handling Modernization

**Exception Handling Enhancement**:
*   **ResourceNotFoundException**: New custom exception for proper HTTP 404 NOT_FOUND responses
*   **GlobalExceptionHandler Enhancement**: Comprehensive exception mapping with proper HTTP status codes
*   **Centralized Error Management**: Single exception handler for all API exceptions with consistent error responses
*   **Exception-Based Controller Flow**: Controllers throw exceptions instead of manually constructing error responses

**API Structure Modernization**:
*   **Complete v1 API Elimination**: Final removal of EventParticipationController from `/api/v1/events` endpoints
*   **Clean v2/v3 Architecture**: No remaining legacy v1 controllers or endpoints in codebase
*   **DTO Reorganization**: TelegramUserDetails moved from API layer to data layer for proper architectural layering
*   **Jackson Integration**: Added `@JsonProperty("name")` annotation for proper JSON serialization

**Code Quality Improvements**:
*   **Proper HTTP Status Codes**: Standardized error responses with correct HTTP codes (404 NOT_FOUND vs custom messages)
*   **Code Deduplication**: Eliminated manual error response construction across controllers
*   **Architectural Clarity**: Clear separation between API responses and internal data transfer objects
*   **Maintainability**: Centralized exception handling logic reduces code duplication

### API v3 Features and Development (November 2025)

### Enhanced User Settings API
*   **Granular Endpoints**: Four dedicated PUT endpoints for individual settings updates:
  - `PUT /api/v2/user/settings/default-max-participants` with `DefaultMaxParticipantsRequest`
  - `PUT /api/v2/user/settings/payment-gateway-url` with `PaymentGatewayUrlRequest`
  - `PUT /api/v2/user/settings/show-become-organizer` with `ShowBecomeOrganizerRequest`
  - `PUT /api/v2/user/settings` comprehensive update with `UserSettingsRequest`
*   **Enhanced DTOs**: Updated request/response DTOs with comprehensive validation (@Min, @Max, @NotNull)
*   **Backward Compatibility**: Maintained existing broader update endpoint with enhanced functionality

### User Statistics and Profile API
*   **User Stats Endpoint**: `GET /api/v2/users/{id}/stats` with comprehensive statistics:
  - Overall rating calculation with decimal precision
  - Review distribution by rating (1-5 scale) with guaranteed key presence
  - Event participation counts (organized events, participated events)
*   **User Details Enhancement**: `GET /api/v2/users/{id}` with enhanced profile information:
  - Avatar image URL support
  - Contact information display
  - Comprehensive user profile data
*   **API Documentation**: Complete API_CONTRACT_V3.md with JSON examples and response formats

## Development & Deployment

*   **Local Environment**: Docker and Docker Compose for PostgreSQL. Gradle wrapper for application execution with dev profile support.
*   **Configuration**: `application.yml` with profile-specific overrides. Environment variables for sensitive data with proper validation.
*   **Dev Profile**: Enables Swagger UI and detailed API documentation with enhanced logging and debugging
*   **Deployment**: Docker-based deployment with GitHub Actions CI/CD, **optimized for Podman** with environment tracking

## Architectural Evolution (November 2025)

### Notification System Architecture - Refactored & Enhanced
The notification system has been completely refactored with a sophisticated unified architecture:

1. **Unified Strategy Pattern Implementation**:
   - `NotificationStrategy` interface with shared and channel-specific methods
   - **Enhanced Interface**: 
     - Shared methods: `getType()`, `isValid(Event, String)`, `getRecipients(Event, String)`
     - Telegram-specific: `createTelegramParams()`, `formatTelegramMessage()`
     - In-App-specific: `createInAppNotification()`
   - `StrategyRegistry` for centralized strategy management and dependency injection
   - **Enhanced Implementations**: All five strategies updated to implement unified interface:
     - `EventCreatedStrategy`, `EventCancelledStrategy`, `EventFinishedStrategy`
     - `ParticipantRegisteredStrategy`, `ParticipantUnregisteredStrategy`

2. **Processor Modernization**:
   - `InAppNotificationProcessor`: Enhanced to inject `StrategyRegistry` and delegate logic with scheduled processing
   - `TelegramNotificationProcessor`: Updated to use new unified method names with proper error handling
   - **Code Quality**: Removed hardcoded recipient determination and DTO creation logic
   - **Architecture**: Clean separation of concerns - processors orchestrate, strategies implement business logic
   - **Reliability**: Scheduled processing with configurable delays and comprehensive progress tracking

3. **Template Engine** (Enhanced):
   - Custom `TemplateEngine` with regex-based variable processing
   - Support for complex expressions: `{{variable|fallback:50}}`
   - Conditional content rendering with `{#if}` blocks
   - Automatic data context building from events and users
   - **Russian Language Support**: Proper encoding and template processing for Russian language templates

4. **Message Formatting Pipeline** (Enhanced):
   - `TelegramMarkdownV2Formatter`: Sophisticated escaping for special characters with URL preservation
   - URL preservation for plain links while escaping markdown formatting
   - Channel-specific formatting: Telegram vs in-app text processing with proper character handling

5. **Template Registry** (Enhanced):
   - `MessageTemplateRegistry` with `@PostConstruct` initialization
   - Default Russian-language templates for all notification types
   - Type-based template selection (TELEGRAM, IN_APP) with fallback handling

### Security Modernization and API Cleanup (November 2025)
*   **v1 API Removal**: Complete elimination of legacy API endpoints for cleaner architecture
*   **Method-Level Security**: Implementation of @PreAuthorize annotations with EventSecurity service
*   **Security Configuration**: Enhanced with @EnableMethodSecurity annotation for method-level security support
*   **EventSecurity Service**: New dedicated service with isEventAuthor() method for centralized authorization
*   **Declarative Security**: Modern security approach preparing for API v3 evolution with centralized logic

### API Design Evolution
*   **v2 API Enhancement**: Multi-participant registration with `EventRegisterRequest` (1-10 participants)
*   **User Settings API**: Granular PUT endpoints for individual settings fields with API v3 enhancements
*   **State Management API**: **NEW**: `UpdateParticipantStateRequest` for managing participant state transitions
*   **Response Consistency**: All v2/v3 endpoints use `ApiResponse<T>` wrapper pattern with comprehensive error handling
*   **Validation**: Comprehensive validation with `@Min`, `@Max`, `@NotNull` annotations on all DTOs
*   **Enhanced User Endpoints**: User details and comprehensive statistics with API v3 features

### Data Model Enhancements (16 Migrations)
*   **UserSettings Model**: Extended with `paymentGatewayUrl` and `showBecomeOrganizer` fields with proper defaults
*   **Notification Progress**: `NotificationProcessorProgress` for tracking processing state and retry mechanisms
*   **Event Logging**: Enhanced event tracking with `EventLog` entity and comprehensive state management
*   **Persistable Pattern**: All entities implement `Persistable<T>` with version control and optimistic locking
*   **Event Sorting Support**: Repository layer enhanced with dynamic ORDER BY clause building based on Pageable sort parameters
*   **Event State Management**: **NEW**: Added `send_poll_link`, `donation_cash`, `donation_transfer` boolean fields to events table
*   **Participant State Management**: **NEW**: Added `poll_link_sent`, `cash_donated`, `transfer_donated` boolean fields to event_participants table
*   **State Management API**: **NEW**: `UpdateParticipantStateRequest` DTO for managing participant state transitions
*   **Database Migration**: Liquibase changelog `db.changelog-2.4-add-event-action-states.xml` for comprehensive state tracking
*   **SQL Optimization**: **NEW**: Enhanced query construction with StringBuilder and Map-based parameter binding
*   **JSON Serialization**: **NEW**: Jackson ObjectMapper integration for proper event log payload serialization
*   **Performance Improvements**: Optimized SQL queries and parameter handling for better database performance

### Testing & Quality Assurance (Enhanced November 2025)
*   **Testcontainers Integration**: Real PostgreSQL instances for integration testing with comprehensive coverage
*   **Strategy Testing**: Dedicated test classes for notification strategies with unified interface validation
*   **Template Engine Testing**: Comprehensive tests for variable processing, conditionals, and Russian language support
*   **Repository Testing**: Enhanced with `EventProjectionRepositoryTest` for sorting functionality validation
*   **Security Testing**: Method-level security testing with @PreAuthorize and EventSecurity service validation
*   **API v3 Testing**: Comprehensive testing of enhanced user settings and user statistics endpoints
*   **State Management Testing**: Testing of event and participant state transitions with atomic updates

## Performance & Monitoring (Enhanced November 2025)
*   **Database Optimization**: 16+ Liquibase changelogs with performance indexes and comprehensive state management
*   **Metrics Collection**: Micrometer with Prometheus integration for comprehensive observability and alerting
*   **Health Monitoring**: Multi-level health checks (database, disk space, application state, liveness/readiness)
*   **Processing Tracking**: Notification processing progress tracking for reliability and retry mechanisms
*   **SQL Query Optimization**: Dynamic query construction with StringBuilder and Map-based parameter binding
*   **JSON Processing**: Jackson ObjectMapper for proper serialization and data integrity

## Security & Authentication (Enhanced November 2025)
*   **Telegram Integration**: Custom authentication filter and provider with enhanced security flow
*   **Stateless Sessions**: No session management, all authentication via Telegram init data with proper validation
*   **CORS Configuration**: Specific origins and methods whitelisted with enhanced security headers
*   **API Security**: All `/api/v2/**` endpoints require authentication with method-level security
*   **Method-Level Security**: **ENHANCED**: @PreAuthorize annotations with EventSecurity service for centralized authorization
*   **Authorization Service**: Dedicated EventSecurity service for event-specific authorization logic with database validation
*   **Security Configuration**: Enhanced with @EnableMethodSecurity for method-level security support
*   **Legacy API Removal**: **COMPLETED**: Complete removal of v1 API endpoints with EventParticipationController elimination
*   **Security Testing**: Comprehensive testing of security implementations and edge cases

## Database Evolution (16 Migrations Summary)

### Core Schema (v1.0-v1.9)
- Initial events, users, participation, reviews, notifications
- Event logs and notification processing progress tracking
- Performance indexes and query optimization

### Enhanced Features (v2.0-v2.4)
- **v2.0**: Performance indexes for query optimization and user settings access
- **v2.1**: User settings enhancement with payment gateway URL and organizer advertisement preferences
- **v2.2**: Database constraint adjustments and data integrity improvements
- **v2.3**: Data migration for existing users with backward compatibility
- **v2.4**: Event and participant state management with comprehensive boolean state tracking

### Migration Characteristics
- **Idempotent Design**: All migrations safe to re-run without side effects
- **Default Values**: Proper defaults for all new fields (false for boolean, null for optional)
- **Data Migration**: Existing data handling with backward compatibility preservation
- **Index Strategy**: Performance-oriented indexing for common query patterns
- **State Management**: Comprehensive boolean fields for poll links and donations tracking

## Development Workflow (November 2025)

### Local Development Setup
```bash
# Start infrastructure
docker compose up -d

# Run application with dev profile for Swagger
./gradlew bootRun --args='--spring.profiles.active=dev'

# Access documentation
# Swagger UI: http://localhost:8080/swagger-ui.html
# OpenAPI JSON: http://localhost:8080/api-docs
# API v3 Contract: API_CONTRACT_V3.md (project root)
```

### API v3 Development Process
1. **Requirements Analysis**: Define enhanced user settings and user statistics requirements
2. **DTO Design**: Create comprehensive request/response DTOs with validation
3. **Service Implementation**: Enhance existing services with new functionality
4. **Controller Development**: Add new endpoints with proper security integration
5. **Testing**: Comprehensive testing with Testcontainers and real PostgreSQL
6. **Documentation**: Update API_CONTRACT_V3.md with examples and response formats

### Security Enhancement Process
1. **Security Analysis**: Identify authorization requirements for new endpoints
2. **EventSecurity Integration**: Enhance EventSecurity service with new authorization logic
3. **Method-Level Security**: Implement @PreAuthorize annotations with proper validation
4. **Testing**: Comprehensive security testing with Spring Security Test
5. **Documentation**: Update security patterns and procedures

### Performance Optimization Workflow
1. **Query Analysis**: Identify slow queries and access patterns
2. **SQL Optimization**: Implement dynamic query construction with StringBuilder
3. **Parameter Binding**: Use Map-based named parameters for better performance
4. **Index Strategy**: Add strategic indexes for frequently queried fields
5. **Testing**: Validate performance improvements with realistic data and Testcontainers

This technical context provides comprehensive coverage of the current implementation state, API v3 features, security modernization, database evolution, and development workflows as of November 30, 2025.
