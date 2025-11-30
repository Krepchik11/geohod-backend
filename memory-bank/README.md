# Memory Bank: Geohod Backend

**Last Updated**: November 30, 2025 (Latest commit: Memory Bank Update with API v3 Documentation)

This Memory Bank provides comprehensive documentation of the Geohod Backend project - a sophisticated event management platform with advanced Telegram integration and emerging API v3 capabilities.

## Quick Overview

**Current Focus**: API v3 Development and Memory Bank Modernization - enhanced user settings endpoints, comprehensive API documentation, and complete architectural cleanup with v1 API removal.

**Major Achievements**: 
- Complete notification system refactor with sophisticated templating engine, Telegram MarkdownV2 formatting, and strategy-based architecture
- **COMPLETED**: Event State and Participant State Management with poll link and donation tracking capabilities
- **COMPLETED**: Complete v1 API removal and security modernization with declarative authorization
- **NEW**: API v3 development with enhanced User Settings API and User Statistics endpoints

## Core Files

### 📋 Project Foundation
- [`projectBrief.md`](projectBrief.md) - Project scope, objectives, and technical stack
- [`productContext.md`](productContext.md) - Product vision, user experience, and business context
- [`systemPatterns.md`](systemPatterns.md) - Architecture patterns, design decisions, and technical patterns

### 🎯 Current Work
- [`activeContext.md`](activeContext.md) - Current development focus and recent changes
- [`progress.md`](progress.md) - What's been completed, current status, and next steps
- [`tasks.md`](tasks.md) - Repetitive workflows and implementation patterns

### 🔧 Technical Details
- [`techContext.md`](techContext.md) - Technology stack, dependencies, and configuration

## Key Highlights (November 2025)

### ✅ Major Enhancements Completed
- **Notification System**: Complete rewrite to strategy-based architecture with unified interface
- **Template Engine**: Custom-built template processing with variables and conditionals
- **User Settings**: Granular preferences with payment gateway URL support and organizer advertisement control
- **Multi-Participant Registration**: Support for 1-10 participants in single request
- **Event State Management**: Poll link sending and donation tracking (cash/transfer) for events
- **Participant State Management**: Individual participant state tracking for poll links and donations
- **SQL Optimization**: Enhanced query construction and parameter handling for better performance
- **Raw JSON Properties**: Proper JSON serialization for event log payloads using Jackson ObjectMapper
- **API Cleanup**: **COMPLETED**: Complete removal of legacy v1 API endpoints and modernization of v2 security
- **Security Modernization**: Method-level security with @PreAuthorize annotations and EventSecurity service
- **Database Evolution**: 16 Liquibase migrations including performance indexes and state management
- **Monitoring**: Micrometer + Prometheus integration
- **Testing**: Enhanced Testcontainers integration with real PostgreSQL

### 🚀 Current Development (November 2025)
- **API v3**: Enhanced User Settings API with granular endpoints and User Statistics endpoints
- **Payment Integration**: Payment gateway URL field implementation ready
- **Performance Optimization**: Continuous database and query optimization efforts
- **Event Sorting**: Dynamic sorting by name, date, status, createdAt, updatedAt with proper database column mapping

### 📊 API v3 Features (New)
- **Enhanced User Settings**: Four dedicated PUT endpoints for granular settings updates
- **User Statistics**: Comprehensive user stats with ratings, event counts, and review distribution
- **User Details**: Enhanced user profile retrieval with avatar and contact information
- **API Documentation**: Complete API contract with DTO examples and response formats

## Usage

This Memory Bank is the single source of truth for understanding the project. Every AI agent working on this project should read ALL files at the start of each task.

**Important**: The project has undergone significant changes since the last update. This Memory Bank reflects the current state as of November 30, 2025.

## Architecture Overview

```
API Layer → Service Layer → Data Access Layer
           ↓
     Notification System
    (Strategy + Templates)
```

- **Strategy Pattern**: Notification strategies with type-safe selection and unified interface
- **Template Engine**: Variables, conditionals, fallbacks, and channel formatting
- **Database**: PostgreSQL with 16+ Liquibase migrations including state management
- **API**: v1 (removed), v2 (primary), v3 (emerging with enhanced features)
- **Authentication**: Telegram WebApp init data validation with method-level security
- **State Management**: Event and participant state tracking for poll links and donations

## Development Status

🟢 **Stable**: Core features, user settings, notification template system, security modernization  
🟡 **Active**: API v3 development, payment integration, performance optimizations  
🔵 **Planning**: Enhanced analytics, advanced monitoring, distributed architecture

## Database Evolution (16 Migrations)

- **v1.0-v1.9**: Core features (events, participation, reviews, notifications)
- **v2.0**: Performance indexes for query optimization
- **v2.1**: User settings enhancement with payment gateway URL and organizer advertisement
- **v2.2-v2.3**: Constraint adjustments and data migration
- **v2.4**: Event and participant state management for poll links and donations

## Recent Technical Achievements

- **Method-Level Security**: Complete @PreAuthorize implementation replacing manual checks
- **SQL Optimization**: Dynamic query construction with StringBuilder and Map-based parameters
- **JSON Serialization**: Jackson ObjectMapper integration for proper event log formatting
- **Event Sorting**: Dynamic ORDER BY clause building based on Pageable parameters
- **Strategy Unification**: Complete refactor of notification strategies to unified interface
- **Template System**: Sophisticated template processing with Russian-language support
- **Testcontainers**: Real PostgreSQL integration testing with comprehensive coverage
