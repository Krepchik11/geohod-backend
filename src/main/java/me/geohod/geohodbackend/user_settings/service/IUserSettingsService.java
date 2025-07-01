package me.geohod.geohodbackend.user_settings.service;

import me.geohod.geohodbackend.user_settings.service.dto.UserSettingsDto;
import java.util.UUID;

public interface IUserSettingsService {
    UserSettingsDto getUserSettings(UUID userId);
    UserSettingsDto updateUserSettings(UUID userId, UserSettingsDto userSettingsDto);
} 