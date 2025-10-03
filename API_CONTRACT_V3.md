# API Contract Updates: Geohod Backend v3

## User Settings API

### Endpoints

| Method | Path | Body | Response | Notes |
|--------|------|------|----------|-------|
| PUT | /api/v2/user/settings/default-max-participants | `DefaultMaxParticipantsRequest` | `ApiResponse<UserSettingsResponse>` | Granular update for default max participants |
| PUT | /api/v2/user/settings/payment-gateway-url | `PaymentGatewayUrlRequest` | `ApiResponse<UserSettingsResponse>` | Granular update for payment gateway URL |
| PUT | /api/v2/user/settings/show-become-organizer | `ShowBecomeOrganizerRequest` | `ApiResponse<UserSettingsResponse>` | Granular update for show become organizer flag |
| PUT | /api/v2/user/settings | `UserSettingsRequest` | `ApiResponse<UserSettingsResponse>` | Broader update including new fields |

### DTOs

#### DefaultMaxParticipantsRequest
```json
{
  "defaultMaxParticipants": 10
}
```

#### PaymentGatewayUrlRequest
```json
{
  "paymentGatewayUrl": "https://example.com/pay"
}
```

#### ShowBecomeOrganizerRequest
```json
{
  "showBecomeOrganizer": true
}
```

#### UserSettingsRequest (updated)
```json
{
  "defaultMaxParticipants": 10,
  "paymentGatewayUrl": "https://example.com/pay",
  "showBecomeOrganizer": true,
  "defaultDonationAmount": "10.00"  // Deprecated
}
```

#### UserSettingsResponse (updated)
```json
{
  "defaultMaxParticipants": 10,
  "paymentGatewayUrl": "https://example.com/pay",
  "showBecomeOrganizer": true,
  "defaultDonationAmount": "10.00"  // Deprecated
}
```

### Notes
- Granular endpoints use dedicated request DTOs for specific updates.
- Existing broader `PUT /api/v2/user/settings` now includes new fields.
- `defaultDonationAmount` deprecated and ignored.

## Event Participation API

### Endpoints

| Method | Path | Body | Response | Notes |
|--------|------|------|----------|-------|
| POST | /api/v2/events/{eventId}/register | `EventRegisterRequest` (optional) | `ApiResponse<EventRegisterResponse>` | Supports 1-3 participants; defaults to 1 if no body |

### DTOs

#### EventRegisterRequest (new)
```json
{
  "amountOfParticipants": 3
}
```

### Notes
- `amountOfParticipants` validated @Min(1) and @Max(3); defaults to 1 for backward compatibility.
- Maintains support for body-less requests (registers 1 participant).

## User API

### Endpoints

| Method | Path | Body | Response | Notes |
|--------|------|------|----------|-------|
| GET | /api/v2/users/{id} | None | `ApiResponse<UserDetailsResponse>` | Retrieves user profile |
| GET | /api/v2/users/{id}/stats | None | `ApiResponse<UserStatsResponse>` | Retrieves user statistics |

### DTOs

#### UserDetailsResponse (new)
```json
{
  "name": "John Doe",
  "username": "@johndoe",
  "imageUrl": "https://example.com/avatar.jpg"
}
```

#### UserStatsResponse (new)
```json
{
  "overallRating": 3.9,
  "reviewsCount": 50,
  "eventsCount": 10,
  "eventsParticipantsCount": 200,
  "reviewsByRating": {1: 0, 2: 5, 3: 10, 4: 20, 5: 15}
}
```

### Notes
- Uses placeholder sample data (e.g., overallRating=3.9).
- `reviewsByRating` Map ensures keys 1-5 for consistent serialization.