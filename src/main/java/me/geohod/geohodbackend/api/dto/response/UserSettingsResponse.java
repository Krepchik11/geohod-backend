package me.geohod.geohodbackend.api.dto.response;

/**
 * DTO for responses from GET and PUT API calls related to user settings.
 */
public record UserSettingsResponse(
        @Deprecated String defaultDonationAmount,
        Integer defaultMaxParticipants,
        String paymentGatewayUrl,
        boolean showBecomeOrganizer) {
    public UserSettingsResponse {
    }
}