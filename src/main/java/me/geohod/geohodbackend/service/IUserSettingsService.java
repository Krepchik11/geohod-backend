package me.geohod.geohodbackend.service;

import java.util.UUID;

import me.geohod.geohodbackend.data.dto.UserSettingsDto;

public interface IUserSettingsService {
    UserSettingsDto getUserSettings(UUID userId);

    UserSettingsDto updateUserSettings(UUID userId, UserSettingsDto userSettingsDto);

    UserSettingsDto updateDefaultDonationAmount(UUID userId, String amount);

    UserSettingsDto updateDefaultMaxParticipants(UUID userId, Integer maxParticipants);

    UserSettingsDto updatePaymentGatewayUrl(UUID userId, String paymentGatewayUrl);

    UserSettingsDto updateShowBecomeOrganizer(UUID userId, Boolean showBecomeOrganizer);

    UserSettingsDto updatePhoneNumber(UUID userId, String phoneNumber);
}