package me.geohod.geohodbackend.data.dto;

public record UserSettingsDto(
        String defaultDonationAmount,
        Integer defaultMaxParticipants,
        String paymentGatewayUrl,
        boolean showBecomeOrganizer,
        String phoneNumber) {
    public UserSettingsDto {
    }

    public UserSettingsDto(String defaultDonationAmount, Integer defaultMaxParticipants,
                          String paymentGatewayUrl, String phoneNumber) {
        this(defaultDonationAmount, defaultMaxParticipants, paymentGatewayUrl, true, phoneNumber);
    }
}