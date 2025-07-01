package me.geohod.geohodbackend.user_settings.api.dto;

/**
 * DTO for incoming PUT requests to update user settings.
 */
public record UserSettingsRequest(
    String defaultDonationAmount,
    Integer defaultMaxParticipants
) {
    public UserSettingsRequest {
    }
} 