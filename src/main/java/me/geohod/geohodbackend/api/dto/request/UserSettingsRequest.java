package me.geohod.geohodbackend.api.dto.request;

/**
 * DTO for incoming PUT requests to update user settings.
 */
public record UserSettingsRequest(
        @Deprecated String defaultDonationAmount,
        Integer defaultMaxParticipants,
        String paymentGatewayUrl,
        boolean showBecomeOrganizer) {
    public UserSettingsRequest {
    }
}