package me.geohod.geohodbackend.user_settings.service.dto;

public record UserSettingsDto(
    String defaultDonationAmount,
    Integer defaultMaxParticipants,
    String paymentGatewayUrl,
    Boolean showBecomeOrganizer
) {
    public UserSettingsDto {
    }
}