# Memory Bank: Geohod Backend

**Last Updated**: November 25, 2025 (Latest commit: Remove v1 API & security updates)

This Memory Bank provides comprehensive documentation of the Geohod Backend project - a sophisticated event management platform with advanced Telegram integration.

## Quick Overview

**Current Focus**: Security Modernization and API Cleanup - removing legacy v1 API and implementing method-level security with @PreAuthorize annotations.

**Major Achievements**: 
- Complete notification system refactor with sophisticated templating engine, Telegram MarkdownV2 formatting, and strategy-based architecture
- **COMPLETED**: Event State and Participant State Management with poll link and donation tracking capabilities
- **NEW**: Complete v1 API removal and security modernization with declarative authorization

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
- **Notification System**: Complete rewrite to strategy-based architecture
- **Template Engine**: Custom-built template processing with variables and conditionals
- **User Settings**: Granular preferences with payment gateway URL support
- **Multi-Participant Registration**: Support for 1-10 participants in single request
- **Event State Management**: Poll link sending and donation tracking (cash/transfer) for events
- **Participant State Management**: Individual participant state tracking for poll links and donations
- **SQL Optimization**: Enhanced query construction and parameter handling for better performance
- **Raw JSON Properties**: Proper JSON serialization for event log payloads using Jackson ObjectMapper
- **API Cleanup**: **NEW**: Complete removal of legacy v1 API endpoints and modernization of v2 security
- **Security Modernization**: **NEW**: Method-level security with @PreAuthorize annotations and EventSecurity service
- **Monitoring**: Micrometer + Prometheus integration
- **Testing**: Enhanced Testcontainers integration

### 🚀 Current Development
- **Issue-84-optional-notifications**: Implementing optional notification preferences
- **Payment Integration**: Payment gateway URL field ready for implementation
- **API v3**: Emerging enhanced API endpoints

## Usage

This Memory Bank is the single source of truth for understanding the project. Every AI agent working on this project should read ALL files at the start of each task.

**Important**: The project has undergone significant changes since the last update. This Memory Bank reflects the current state as of November 2025.

## Architecture Overview

```
API Layer → Service Layer → Data Access Layer
           ↓
     Notification System
    (Strategy + Templates)
```

- **Strategy Pattern**: Notification strategies with type-safe selection
- **Template Engine**: Variables, conditionals, fallbacks, and channel formatting
- **Database**: PostgreSQL with 15+ Liquibase migrations
- **API**: v1 (legacy), v2 (primary), v3 (emerging)
- **Authentication**: Telegram WebApp init data validation

## Development Status

🟢 **Stable**: Core features, user settings, notification template system  
🟡 **Active**: Issue-84 optional notifications, payment integration  
🔵 **Planning**: API v3, enhanced analytics, performance optimizations
