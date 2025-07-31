# System Patterns: Geohod Backend

## System Architecture

### High-Level Architecture

The Geohod backend follows a layered architecture pattern with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    API Layer                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │   v1 API    │  │   v2 API    │  │   OpenAPI   │        │
│  │ Controllers │  │ Controllers │  │  Docs       │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                  Service Layer                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │ Event Mgmt  │  │ User Mgmt   │  │ Notification│        │
│  │ Services    │  │ Services    │  │ Services    │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                 Data Access Layer                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │  Entities   │  │ Repositories│  │   Mappers   │        │
│  │   (Models)  │  │   (DAOs)    │  │ (MapStruct) │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                  Infrastructure                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │ PostgreSQL  │  │   Telegram  │  │   Security  │        │
│  │   Database  │  │    Bot      │  │   Layer     │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

### Component Relationships

1. **API Layer**: 
   - Handles HTTP requests/responses
   - Validates input data
   - Maps between DTOs and domain objects
   - Delegates business logic to service layer

2. **Service Layer**:
   - Implements business logic
   - Orchestrates operations across multiple repositories
   - Manages transactions
   - Handles business rule validation

3. **Data Access Layer**:
   - Manages persistence operations
   - Defines entity relationships
   - Implements custom queries
   - Handles database migrations

4. **Infrastructure Layer**:
   - Provides cross-cutting concerns
   - Integrates with external systems
   - Handles security and authentication
   - Manages configuration

## Key Technical Decisions

### 1. API Versioning Strategy
- **Decision**: Implemented API versioning with v1 (legacy) and v2 (current) endpoints
- **Rationale**: Allows for gradual migration without breaking existing clients
- **Implementation**: Separate controller packages for v1 and v2 with distinct request mappings

### 2. Authentication Approach
- **Decision**: Custom authentication based on Telegram init data
- **Rationale**: Leverages Telegram's user base for seamless authentication
- **Implementation**: Custom authentication filter and provider with TelegramPrincipal

### 3. Data Access Strategy
- **Decision**: Spring Data JDBC with repositories
- **Rationale**: Simpler than JPA for this use case, better performance
- **Implementation**: Repository interfaces extending JpaRepository with custom queries

### 4. DTO Mapping Strategy
- **Decision**: MapStruct for automatic DTO mapping
- **Rationale**: Reduces boilerplate code, type-safe mapping
- **Implementation**: Mapper interfaces with automatic component scanning

### 5. Notification System
- **Decision**: Outbox pattern for reliable notifications
- **Rationale**: Ensures reliable delivery even if Telegram is temporarily unavailable
- **Implementation**: TelegramOutboxMessage entity with processor service

## Design Patterns in Use

### 1. Layered Architecture
- Clear separation between API, Service, Data Access, and Infrastructure layers
- Each layer has distinct responsibilities
- Dependencies flow downward

### 2. Repository Pattern
- Abstracts database operations
- Provides collection-like interface to domain objects
- Enables easy testing with mock implementations

### 3. DTO Pattern
- Separate data transfer objects for API communication
- Prevents exposing internal domain model
- Enables API evolution without breaking domain model

### 4. Service Layer Pattern
- Encapsulates business logic
- Provides transaction boundaries
- Coordinates between multiple repositories

### 5. Outbox Pattern
- Ensures reliable notification delivery
- Decouples business operations from notification sending
- Provides retry mechanism for failed notifications

### 6. Strategy Pattern
- Used in notification handling with different notification types
- Allows easy extension of notification strategies
- Enables runtime selection of notification handling logic

## Critical Implementation Paths

### 1. Event Creation Flow
```
EventController → EventService → EventRepository → Database
                ↓
          NotificationService → OutboxMessage → TelegramBot
```

### 2. User Authentication Flow
```
TelegramInitDataFilter → TelegramTokenAuthProvider → TelegramPrincipal
                     ↓
                UserService → UserRepository → Database
```

### 3. Event Participation Flow
```
EventParticipationController → EventParticipationService → EventParticipantRepository
                          ↓
                    EventService → EventRepository
                          ↓
                    NotificationService → OutboxMessage
```

### 4. Notification Processing Flow
```
TelegramOutboxProcessor → TelegramOutboxMessageRepository
                     ↓
               TelegramBotService → Telegram API
                     ↓
               NotificationProcessorProgressRepository
```

## Security Patterns

### 1. Custom Authentication
- Telegram init data validation
- JWT-like token generation
- Stateless authentication

### 2. Authorization
- Method-level security with @PreAuthorize
- Custom access control based on event ownership
- Role-based access control for different operations

### 3. CORS Configuration
- Restricted to specific domains
- Configurable allowed methods and headers
- Credentials support for authenticated requests

## Error Handling Patterns

### 1. Global Exception Handler
- Centralized error handling
- Consistent error response format
- Proper HTTP status codes

### 2. Custom Exceptions
- Domain-specific exceptions
- Clear error messages
- Proper exception hierarchy

### 3. Validation
- Bean Validation annotations
- Custom validators for complex rules
- Early validation at API boundaries