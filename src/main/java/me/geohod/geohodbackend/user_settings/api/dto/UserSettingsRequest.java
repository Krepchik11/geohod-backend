package me.geohod.geohodbackend.user_settings.api.dto;

/**
 * DTO for incoming PUT requests to update user settings.
 */
public record UserSettingsRequest(
    @Deprecated
    String defaultDonationAmount,
    Integer defaultMaxParticipants,
    String paymentGatewayUrl,
    boolean showBecomeOrganizer
) {
    public UserSettingsRequest {
    }
}