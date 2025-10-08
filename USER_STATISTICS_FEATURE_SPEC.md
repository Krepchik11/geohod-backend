# User Statistics Feature Specification

## Overview
The User Statistics feature provides comprehensive statistical information about users in the Geohod platform, including their event participation history, review statistics, and overall performance metrics.

## Feature Status
✅ **IMPLEMENTED** - All functionality is complete and available in the current codebase.

## API Endpoints

### Get User Statistics
- **Endpoint**: `GET /api/v2/users/{id}/stats`
- **Authentication**: Required (Telegram authentication)
- **Response**: `ApiResponse<UserStatsResponse>`

#### Response DTO: UserStatsResponse
```json
{
  "overallRating": 3.9,
  "reviewsCount": 50,
  "eventsCount": 10,
  "eventsParticipantsCount": 200,
  "reviewsByRating": {
    "1": 0,
    "2": 5,
    "3": 10,
    "4": 20,
    "5": 15
  }
}
```

#### Response Fields
- `overallRating` (Double): Average rating from all reviews received
- `reviewsCount` (Integer): Total number of reviews received
- `eventsCount` (Integer): Total number of events created by the user
- `eventsParticipantsCount` (Integer): Total number of participants across all user's events
- `reviewsByRating` (Map<Integer, Integer>): Distribution of ratings (1-5 stars) received

## Service Layer

### IUserStatsService Interface
Located: `src/main/java/me/geohod/geohodbackend/service/IUserStatsService.java`

#### Methods
- `getUserStats(UUID userId)`: Returns complete user statistics
- `getUserEventsCount(UUID userId)`: Returns count of events created by user
- `getUserEventsParticipantsCount(UUID userId)`: Returns total participants across user's events
- `getUserReviewsByRating(UUID userId)`: Returns rating distribution map

### UserStatsServiceImpl Implementation
Located: `src/main/java/me/geohod/geohodbackend/service/impl/UserStatsServiceImpl.java`

#### Dependencies
- `IUserRatingService`: For calculating user ratings
- `EventRepository`: For event-related queries
- `ReviewRepository`: For review-related queries

#### Key Features
- Comprehensive error handling with meaningful exceptions
- Input validation for null user IDs
- Structured logging for debugging and monitoring
- Efficient database queries using repository projections

## Data Access Layer

### Repository Methods
The service uses custom repository methods for efficient data retrieval:

#### EventRepository
- `countByAuthorId(UUID userId)`: Count events created by user
- `sumParticipantsByAuthorId(UUID userId)`: Sum total participants across user's events

#### ReviewRepository
- `countReviewsByRatingForUser(UUID userId)`: Get rating distribution for user

## Controller Implementation

### UserController
Located: `src/main/java/me/geohod/geohodbackend/api/controller/v2/UserController.java`

#### Endpoint Details
- **Path**: `/{id}/stats`
- **Method**: `GET`
- **Operation**: Swagger documented with summary "Get user statistics by ID"
- **Response**: `ResponseEntity<ApiResponse<UserStatsResponse>>`

## Error Handling

### Validation Errors
- `IllegalArgumentException` for null user IDs
- Proper error propagation through service layers

### Runtime Errors
- `RuntimeException` for calculation failures
- Detailed logging for troubleshooting

## Performance Considerations

### Database Queries
- Uses repository projections for efficient data retrieval
- Avoids N+1 query problems through optimized queries
- Leverages Spring Data JDBC for direct SQL control

### Caching Strategy
- No explicit caching implemented (could be added for frequently accessed users)

## Security

### Authentication
- Requires valid Telegram authentication
- Endpoint protected by Spring Security configuration

### Authorization
- No additional authorization checks (statistics are considered public information)

## Testing

### Current Test Coverage
- No specific tests for UserStatsServiceImpl identified
- Integration with existing test infrastructure recommended

### Recommended Tests
- Unit tests for service layer calculations
- Integration tests for database queries
- API endpoint tests for response format

## API Documentation

### OpenAPI/Swagger
- Endpoint automatically documented through annotations
- Available in development profile at `/swagger-ui.html`
- Response schema matches UserStatsResponse record structure

## Future Enhancements

### Potential Improvements
1. **Caching**: Add Redis caching for frequently accessed statistics
2. **Real-time Updates**: WebSocket support for live statistic updates
3. **Historical Data**: Track statistics over time periods
4. **Comparative Analytics**: Compare user statistics with platform averages
5. **Export Functionality**: Allow users to export their statistics

### Scalability Considerations
- Database indexes on frequently queried columns
- Consider read replicas for heavy analytics workloads
- Pagination support if statistics become more complex

## Deployment Notes

### Database Migrations
- No additional migrations required (uses existing tables)
- Leverages existing User, Event, Review, and EventParticipant tables

### Configuration
- No additional configuration required
- Uses existing application properties and dependencies

## Monitoring and Observability

### Logging
- Service methods include structured logging
- Error scenarios properly logged with context
- Performance metrics can be added for query timing

### Metrics
- Consider adding Micrometer metrics for:
  - Response times
  - Cache hit rates (if implemented)
  - Error rates

## Dependencies

### Compile-time Dependencies
- Spring Boot Starter Web
- Lombok (for logging and constructor generation)
- MapStruct (for DTO mapping)

### Runtime Dependencies
- PostgreSQL driver
- Spring Data JDBC

## File Structure
```
src/main/java/me/geohod/geohodbackend/
├── service/
│   ├── IUserStatsService.java
│   └── impl/
│       └── UserStatsServiceImpl.java
├── api/
│   ├── controller/v2/UserController.java
│   └── dto/response/UserStatsResponse.java
└── data/model/repository/
    ├── EventRepository.java
    └── ReviewRepository.java
```

## Integration Points

### Related Features
- **User Management**: Core user data retrieval
- **Event Management**: Event creation and participation tracking
- **Review System**: Rating calculations and review aggregation
- **Authentication**: Telegram-based user identification

### External Dependencies
- **Telegram Bot API**: Indirect dependency through authentication system
- **Database**: PostgreSQL for data persistence

## Success Criteria

✅ **Completed**
- [x] Complete user statistics calculation service
- [x] RESTful API endpoint with proper response format
- [x] Integration with existing user, event, and review systems
- [x] Proper error handling and validation
- [x] API documentation and Swagger integration
- [x] Database query optimization

## Maintenance Notes

### Code Quality
- Follows existing project patterns and conventions
- Proper separation of concerns between layers
- Comprehensive error handling
- Input validation at service boundaries

### Future Refactoring
- Consider extracting complex calculation logic into separate utility classes
- Add configuration options for statistics calculation parameters
- Implement caching layer for improved performance