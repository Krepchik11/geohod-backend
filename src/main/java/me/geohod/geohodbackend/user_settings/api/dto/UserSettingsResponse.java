package me.geohod.geohodbackend.user_settings.api.dto;

/**
 * DTO for responses from GET and PUT API calls related to user settings.
 */
public record UserSettingsResponse(
    String defaultDonationAmount,
    Integer defaultMaxParticipants
) {
    public UserSettingsResponse {
    }
} 