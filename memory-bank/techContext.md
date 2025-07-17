# Technical Context: Geohod Backend

## Core Technologies

*   **Java Version**: 21+ (as specified in `build.gradle`, currently configured for Java 23)
*   **Spring Boot**: 3.3.5
*   **Build Tool**: Gradle 8.1+
*   **Database**: PostgreSQL
*   **Web Server**: Embedded Tomcat (default for `spring-boot-starter-web`)

## Key Dependencies & Libraries

*   **Data Access**: `spring-boot-starter-data-jdbc`. This indicates a direct JDBC-based approach for database interactions, rather than a full-fledged ORM like Hibernate (JPA).
*   **Security**: `spring-boot-starter-security` for handling authentication and authorization. The security model is custom, built around Telegram authentication.
*   **Web**: `spring-boot-starter-web` for building RESTful APIs.
*   **Database Migrations**: `liquibase-core` is used to manage and apply database schema changes automatically on startup.
*   **Telegram Integration**: `telegrambots-spring-boot-starter` provides the core integration with the Telegram Bot API.
*   **API Documentation**: `springdoc-openapi-starter-webmvc-ui` generates OpenAPI 3.0 documentation and provides the Swagger UI.
*   **Development Tooling**:
    *   `lombok`: Reduces boilerplate code for models and other classes.
    *   `spring-boot-docker-compose`: Simplifies local development by managing Docker containers.
    *   `mapstruct`: A code generator for type-safe bean mappings between DTOs and entities.
*   **Testing**:
    *   `spring-boot-starter-test` (includes JUnit 5, Mockito, etc.).
    *   `spring-security-test`: For testing security configurations.
    *   `testcontainers`: For running integration tests against a real PostgreSQL database in a Docker container.

## Development & Deployment

*   **Local Environment**: Requires Docker and Docker Compose to run a local PostgreSQL instance. The application can be run directly via the Gradle wrapper (`./gradlew bootRun`).
*   **Configuration**: The application uses `application.yml` and supports profile-specific configurations (e.g., `application-dev.yml`). The `dev` profile is used to enable Swagger UI.
*   **Deployment**: The project is designed for Docker-based deployment. A `Dockerfile` is provided, and GitHub Actions are set up for automated builds and deployments to a VPS.

## Architectural Notes

*   The use of `spring-data-jdbc` suggests a focus on simpler data mapping and more direct control over SQL, compared to JPA/Hibernate.
*   The project uses MapStruct for mapping between data layers (e.g., DTOs to domain models), which is a compile-time dependency that helps ensure type safety and performance.
*   Authentication is a critical and custom component, tightly coupled with Telegram's authentication flow.
