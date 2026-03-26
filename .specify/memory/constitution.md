<!-- Sync Impact Report: Constitution updated with specific Java Spring Boot tech stack
- Version bumped to 1.2.0 (MINOR): Added detailed technology stack specifications and version constraints
- Specified Java 23, Spring Boot 3.5.6, Gradle 8.x, PostgreSQL, Spring Data JDBC requirements
- Added specific tooling requirements: Liquibase, Spring Security, springdoc-openapi, MapStruct, Lombok, Actuator
- Updated Core Principles to include technology-specific architectural constraints
- Enhanced database and security requirements with specific tool implementations
- Updated all dependent templates to reflect specific technology stack requirements
- All templates synchronized with version 1.2.0
-->
# Geohod Backend Spicy Constitution

## Core Principles

### Structured Development Workflow
All development must follow the speckit workflow process including constitution governance, feature specifications, implementation plans, and task management. Every feature requires a complete specification with user stories, requirements, and success criteria before implementation begins.

### Constitution-Driven Governance
The project constitution serves as the supreme governing document. All templates, processes, and development activities must align with constitutional principles. Amendments require version tracking and propagation across all dependent templates and documentation.

### Template Standardization
All project artifacts (specifications, plans, tasks, checklists) must use approved templates from `.specify/templates/`. Templates ensure consistency, completeness, and compliance with project standards across all development activities.

### Feature Independence & Testability
Each feature must be developed as an independent, testable unit. Features should be prioritized and implemented in slices that deliver standalone value. All features require independent testing capabilities and clear acceptance criteria.

### Documentation-First Approach
Documentation must precede implementation. Every feature requires comprehensive specifications, technical context, and success criteria before development begins. All project artifacts must maintain consistency across templates and documentation.

### Java Architecture Principles
All Java code must adhere to SOLID principles (Single Responsibility, Open-Closed, Liskov Substitution, Interface Segregation, Dependency Inversion). Maintain high cohesion and low coupling throughout the application architecture.

### Spring Boot Best Practices
Follow Spring Boot conventions and best practices. Structure applications with clear separation of concerns using controllers, services, repositories, models (entities), and configurations. Implement proper dependency injection and configuration management.

### Clean Code Standards
Write clean, efficient, self-documented Java code. Ensure clear, concise, and well-formatted code with meaningful variable and method names. Favor simple solutions over complex ones (KISS principle). Avoid code duplication (DRY principle). Implement only necessary features (YAGNI principle).

### RESTful API Design
Implement RESTful API design patterns with proper HTTP methods (GET, POST, PUT, DELETE, PATCH), status codes, and resource naming conventions. Use appropriate content types and provide comprehensive API documentation.

### Data Transfer Objects (DTOs)
Return DTOs, not entities, from service implementation methods unless absolutely necessary. Use dedicated DTOs for API responses and requests. Use `record` types for immutable data transfer objects unless specified otherwise.

### Database Management
Implement proper database indexing and query optimization for performance. Properly manage database transactions for data consistency. Consider idempotency for critical operations. All database operations must be performed using repository methods. Do not use JPA - use direct database access patterns.

### Security Compliance
Adhere to OWASP best practices for secure coding. Implement proper input validation, output encoding, authentication, authorization, and protection against common vulnerabilities (XSS, CSRF, SQL injection, etc.).

## Technology Stack Requirements

### Core Technologies
All Java implementations must use **Java 23** with Gradle toolchain. Spring Boot framework version **3.5.6** is mandatory for all applications. Gradle **8.x** is required for build automation and dependency management.

### Persistence Layer
Use **Spring Data JDBC** for data access operations. **PostgreSQL** is the required database for all persistence needs. **Liquibase** must be used for database schema versioning and migrations.

### Security Implementation
**Spring Security** is mandatory for authentication, authorization, and security configurations. Implement comprehensive security measures including JWT tokens, role-based access control, and security event logging.

### API Documentation
Use **springdoc-openapi-starter-webmvc-ui** for generating OpenAPI 3 documentation. All REST endpoints must be documented with proper schemas, examples, and parameter descriptions.

### Data Mapping
**MapStruct 1.5.5.Final** is required for efficient object mapping between DTOs and entities. Manual mapping is prohibited to ensure type safety and performance.

### Development Utilities
**Lombok** is mandatory for reducing boilerplate code. Use appropriate annotations for getters, setters, constructors, and builders while maintaining code readability.

### Monitoring & Observability
**Spring Boot Actuator** is required for application monitoring, health checks, metrics collection, and operational visibility. Implement custom health indicators for domain-specific checks.

## Development Workflow
Technology-agnostic development process templates and commands that can be applied to any programming language or project type (single project, web applications, mobile + API).

## Template Management
Centralized template system in `.specify/templates/` that ensures consistency across all project documentation and development artifacts.

## Governance

Constitution Supremacy: This constitution supersedes all other development practices and documentation. All project templates and workflows must align with these principles. Amendment Process: Changes to the constitution require: - Version increment following semantic versioning (MAJOR.MINOR.PATCH) - Update of all dependent templates in `.specify/templates/` - Propagation of changes to existing project artifacts - Documentation of rationale for changes. Compliance Verification: All features, specifications, and implementations must be validated against constitutional principles before approval. Template Consistency: Any changes to constitutional principles require review and update of all template files to maintain alignment.

**Version**: 1.2.0 | **Ratified**: 2025-10-07 | **Last Amended**: 2025-10-07