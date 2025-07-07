package me.geohod.geohodbackend.user_settings.service;

import me.geohod.geohodbackend.user_settings.data.model.UserSettings;
import me.geohod.geohodbackend.user_settings.data.repository.UserSettingsRepository;
import me.geohod.geohodbackend.user_settings.mapper.UserSettingsMapper;
import me.geohod.geohodbackend.user_settings.service.dto.UserSettingsDto;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserSettingsServiceImpl implements IUserSettingsService {
    private final UserSettingsRepository userSettingsRepository;
    private final UserSettingsMapper userSettingsMapper;

    public UserSettingsServiceImpl(UserSettingsRepository userSettingsRepository, UserSettingsMapper userSettingsMapper) {
        this.userSettingsRepository = userSettingsRepository;
        this.userSettingsMapper = userSettingsMapper;
    }

    @Override
    public UserSettingsDto getUserSettings(UUID userId) {
        Optional<UserSettings> settingsOpt = userSettingsRepository.findByUserId(userId);
        return settingsOpt
                .map(userSettingsMapper::toDto)
                .orElse(new UserSettingsDto(null, null));
    }

    @Override
    public UserSettingsDto updateUserSettings(UUID userId, UserSettingsDto userSettingsDto) {
        Optional<UserSettings> settingsOpt = userSettingsRepository.findByUserId(userId);
        UserSettings settings = settingsOpt.orElseGet(() -> {
            UserSettings newSettings = userSettingsMapper.toEntity(userSettingsDto);
            newSettings.setUserId(userId);
            return newSettings;
        });
        settings.updateSettings(userSettingsDto.defaultDonationAmount(), userSettingsDto.defaultMaxParticipants());
        UserSettings saved = userSettingsRepository.save(settings);
        return userSettingsMapper.toDto(saved);
    }
} 