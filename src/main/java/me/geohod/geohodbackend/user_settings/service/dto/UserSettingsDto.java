package me.geohod.geohodbackend.user_settings.service.dto;

public record UserSettingsDto(String defaultDonationAmount, Integer defaultMaxParticipants) {
    public UserSettingsDto {
    }
} 