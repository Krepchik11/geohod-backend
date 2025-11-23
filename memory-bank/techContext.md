# Technical Context: Geohod Backend

## Core Technologies

*   **Java Version**: 23 (recently updated from Java 21+)
*   **Spring Boot**: 3.3.5
*   **Build Tool**: Gradle 8.x
*   **Database**: PostgreSQL
*   **Web Server**: Embedded Tomcat (default for `spring-boot-starter-web`)

## Key Dependencies & Libraries (Updated November 2025)

### Core Dependencies
*   **Data Access**: `spring-boot-starter-data-jdbc`. Direct JDBC-based approach for database interactions.
*   **Security**: `spring-boot-starter-security` with custom Telegram authentication flow.
*   **Web**: `spring-boot-starter-web` for building RESTful APIs.
*   **Database Migrations**: `liquibase-core` for automated schema management (15+ changelog files).
*   **Telegram Integration**: `telegrambots-spring-boot-starter:6.9.7.1` for Telegram Bot API integration.

### Enhanced Monitoring & Metrics
*   **Monitoring**: `spring-boot-starter-actuator` with `micrometer-registry-prometheus` integration
*   **Metrics**: Comprehensive metrics collection for application performance and business metrics
*   **Health Checks**: Enhanced health endpoints including liveness and readiness states

### Message Processing & Templating
*   **Template Engine**: Custom-built template engine supporting:
  - Variable interpolation: `{{variable}}`, `{{variable|fallback}}`, `{{variable:50}}`
  - Conditional blocks: `{#if condition}content{/if}`
  - Automatic fallback values for missing variables
*   **Message Formatting**: `TelegramMarkdownV2Formatter` with sophisticated character escaping
*   **Template Registry**: `MessageTemplateRegistry` for managing notification templates by type and ID

### Development & Testing Tooling
*   **lombok**: Reduces boilerplate code for models and other classes
*   **mapstruct:1.5.5.Final**: Code generator for type-safe bean mappings
*   **API Documentation**: `springdoc-openapi-starter-webmvc-ui:2.4.0` for OpenAPI 3.0 and Swagger UI
*   **Testing Suite**:
  *   `spring-boot-starter-test` (JUnit 5, Mockito)
  *   `spring-security-test`: For security configuration testing
  *   **Testcontainers**: `testcontainers:junit-jupiter:1.19.7` and `testcontainers:postgresql:1.19.7` for integration testing

### New Notification System Architecture
*   **Strategy Pattern**: `StrategyRegistry` with `NotificationStrategy` implementations
*   **Template System**: `MessageTemplate` records with variable, fallback, and type support
*   **Channel Formatting**: Separate formatters for Telegram (MarkdownV2) and in-app (plain text)

## Development & Deployment

*   **Local Environment**: Docker and Docker Compose for PostgreSQL. Gradle wrapper for application execution.
*   **Configuration**: `application.yml` with profile-specific overrides. Environment variables for sensitive data.
*   **Dev Profile**: Enables Swagger UI and detailed API documentation
*   **Deployment**: Docker-based deployment with GitHub Actions CI/CD, **optimized for Podman**

## Architectural Evolution (November 2025)

### Notification System Architecture
The notification system has been completely refactored with a sophisticated architecture:

1. **Strategy Pattern Implementation**:
   - `StrategyNotificationType` enum (replacing deprecated `NotificationType`)
   - `StrategyRegistry` for dependency injection and strategy management
   - Dedicated strategy classes: `EventCreatedStrategy`, `EventCancelledStrategy`, etc.

2. **Template Engine**:
   - Custom `TemplateEngine` with regex-based variable processing
   - Support for complex expressions: `{{variable|fallback:50}}`
   - Conditional content rendering with `{#if}` blocks
   - Automatic data context building from events and users

3. **Message Formatting Pipeline**:
   - `TelegramMarkdownV2Formatter`: Sophisticated escaping for special characters
   - URL preservation for plain links while escaping markdown formatting
   - Channel-specific formatting: Telegram vs in-app text processing

4. **Template Registry**:
   - `MessageTemplateRegistry` with `@PostConstruct` initialization
   - Default Russian-language templates for all notification types
   - Type-based template selection (TELEGRAM, IN_APP)

### API Design Evolution
*   **v2 API Enhancement**: Multi-participant registration with `EventRegisterRequest` (1-10 participants)
*   **User Settings API**: Granular PUT endpoints for individual settings fields
*   **Response Consistency**: All v2 endpoints use `ApiResponse<T>` wrapper pattern
*   **Validation**: Comprehensive validation with `@Min`, `@Max`, `@NotNull` annotations

### Data Model Enhancements
*   **UserSettings Model**: Extended with `paymentGatewayUrl` and `showBecomeOrganizer` fields
*   **Notification Progress**: `NotificationProcessorProgress` for tracking processing state
*   **Event Logging**: Enhanced event tracking with `EventLog` entity and `EventType` enum
*   **Persistable Pattern**: All entities implement `Persistable<T>` with version control

### Testing & Quality Assurance
*   **Testcontainers Integration**: Real PostgreSQL instances for integration testing
*   **Strategy Testing**: Dedicated test classes for notification strategies and formatters
*   **Template Engine Testing**: Comprehensive tests for variable processing and conditionals

## Performance & Monitoring
*   **Database Optimization**: 15+ Liquibase changelogs with performance indexes
*   **Metrics Collection**: Micrometer with Prometheus integration for observability
*   **Health Monitoring**: Multi-level health checks (database, disk space, application state)
*   **Processing Tracking**: Notification processing progress tracking for reliability

## Security & Authentication
*   **Telegram Integration**: Custom authentication filter and provider
*   **Stateless Sessions**: No session management, all authentication via Telegram init data
*   **CORS Configuration**: Specific origins and methods whitelisted
*   **API Security**: All `/api/v2/**` endpoints require authentication
