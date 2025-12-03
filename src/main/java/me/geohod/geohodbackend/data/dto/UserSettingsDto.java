package me.geohod.geohodbackend.data.dto;

public record UserSettingsDto(
        String defaultDonationAmount,
        Integer defaultMaxParticipants,
        String paymentGatewayUrl,
        Boolean showBecomeOrganizer,
        String phoneNumber) {
    public UserSettingsDto {
    }
}