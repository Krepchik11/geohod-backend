# Tech Context: Geohod Backend

## Technology Stack

### Core Technologies
- **Language**: Java 23
- **Framework**: Spring Boot 3.3.5
- **Build Tool**: Gradle 8.x
- **Database**: PostgreSQL
- **Database Migrations**: Liquibase
- **Authentication**: Custom Telegram-based authentication

### Key Dependencies
- **Spring Boot Starters**:
  - `spring-boot-starter-data-jdbc`: Data access with JDBC
  - `spring-boot-starter-security`: Security framework
  - `spring-boot-starter-web`: Web framework
  - `spring-boot-starter-actuator`: Monitoring and management

- **Telegram Integration**:
  - `telegrambots-spring-boot-starter:6.9.7.1`: Telegram Bot API integration

- **API Documentation**:
  - `springdoc-openapi-starter-webmvc-ui:2.4.0`: OpenAPI/Swagger documentation

- **Database**:
  - `postgresql`: PostgreSQL JDBC driver

- **Utilities**:
  - `liquibase-core`: Database schema migrations
  - `mapstruct:1.5.5.Final`: Type-safe bean mapping
  - `lombok`: Boilerplate code reduction

- **Testing**:
  - `spring-boot-starter-test`: Spring Boot testing utilities
  - `spring-security-test`: Security testing utilities
  - `testcontainers:junit-jupiter:1.19.7`: Integration testing with containers
  - `testcontainers:postgresql:1.19.7`: PostgreSQL test container

## Development Setup

### Prerequisites
- Java 23 or later
- Docker and Docker Compose
- PostgreSQL database
- Gradle build tool

### Local Development Environment
1. **Database Setup**:
   - Use Docker Compose for local PostgreSQL instance
   - Database migrations handled by Liquibase on application startup

2. **Application Configuration**:
   - Environment variables stored in `.env` file
   - Template provided in `.env.template`
   - Configuration properties managed through `@ConfigurationProperties`

3. **Running the Application**:
   - Use Gradle wrapper: `./gradlew bootRun`
   - Or run directly from IDE
   - Actuator endpoints available for monitoring

### Build and Deployment
- **Build**: `./gradlew build`
- **Test**: `./gradlew test`
- **Docker**: Multi-stage Dockerfile for production builds
- **CI/CD**: GitHub Actions for automated deployment

## Technical Constraints

### Performance Requirements
- API response times under 500ms for 95% of requests
- Support for concurrent users with proper connection pooling
- Efficient database queries with proper indexing

### Security Requirements
- All API endpoints (except documentation and actuator) require authentication
- CORS restrictions to specific domains
- Telegram init data validation for secure authentication
- Stateless session management

### Data Integrity
- Database transactions for critical operations
- Proper foreign key constraints
- Data validation at multiple layers (API, Service, Database)

### Scalability Considerations
- Stateless design for horizontal scaling
- Database connection pooling
- Pagination for large result sets
- Efficient notification processing with outbox pattern

## Configuration Management

### Environment-Specific Configuration
- Application properties in `application.yml`
- Environment-specific overrides
- Sensitive data stored in environment variables

### Key Configuration Properties
- Database connection settings
- Telegram bot configuration
- Security settings (CORS, authentication)
- Liquibase migration settings
- Actuator endpoints configuration

### External Service Integration
- Telegram Bot API for notifications
- PostgreSQL database for data persistence
- Optional: External monitoring and logging services

## Tool Usage Patterns

### Code Quality and Style
- **Lombok**: Reduces boilerplate code for getters, setters, constructors
- **MapStruct**: Type-safe mapping between DTOs and entities
- **Spring Boot Annotations**: Consistent use of Spring annotations for dependency injection, configuration

### Database Patterns
- **Liquibase**: Version-controlled database schema changes
- **Spring Data JDBC**: Repository pattern for data access
- **DTO Pattern**: Separation between API models and database entities

### API Design Patterns
- **RESTful Design**: Proper HTTP methods, status codes, and resource naming
- **API Versioning**: Separate v1 and v2 endpoints for backward compatibility
- **OpenAPI Documentation**: Auto-generated API documentation
- **Consistent Response Format**: ApiResponse wrapper for all responses

### Security Patterns
- **Custom Authentication**: Telegram-based authentication with custom filters
- **Method Security**: Authorization at method level with Spring Security
- **CORS Configuration**: Restricted cross-origin requests

### Testing Patterns
- **Unit Testing**: JUnit 5 with Mockito for service layer testing
- **Integration Testing**: TestContainers for database-dependent tests
- **Spring Security Test**: Testing security-aware endpoints

## Development Workflow

### Code Organization
- Layered architecture with clear package structure
- Separate packages for v1 and v2 API controllers
- Dedicated DTO packages for request/response objects
- Service interfaces and implementations separated

### Version Control
- Git for version control
- Feature branch workflow
- Pull requests for code review
- Semantic versioning for releases

### CI/CD Pipeline
- Automated builds on GitHub Actions
- Automated testing with multiple Java versions
- Docker image building and pushing
- Deployment to staging/production environments

## Monitoring and Observability

### Application Monitoring
- Spring Boot Actuator endpoints
- Health checks, metrics, and environment information
- Custom metrics for business operations

### Logging
- Structured logging with consistent format
- Log levels appropriate for different environments
- Request/response logging for debugging

### Error Tracking
- Global exception handling
- Consistent error response format
- Proper HTTP status codes for different error scenarios