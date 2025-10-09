package me.geohod.geohodbackend.user_settings.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.geohod.geohodbackend.user_settings.data.model.UserSettings;
import me.geohod.geohodbackend.user_settings.data.repository.UserSettingsRepository;
import me.geohod.geohodbackend.user_settings.mapper.UserSettingsMapper;
import me.geohod.geohodbackend.user_settings.service.dto.UserSettingsDto;

@Service
@Transactional
public class UserSettingsServiceImpl implements IUserSettingsService {
    private final UserSettingsRepository userSettingsRepository;
    private final UserSettingsMapper userSettingsMapper;

    public UserSettingsServiceImpl(UserSettingsRepository userSettingsRepository, UserSettingsMapper userSettingsMapper) {
        this.userSettingsRepository = userSettingsRepository;
        this.userSettingsMapper = userSettingsMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserSettingsDto getUserSettings(UUID userId) {
        Optional<UserSettings> settingsOpt = userSettingsRepository.findByUserId(userId);
        return settingsOpt
                .map(userSettingsMapper::toDto)
                .orElse(new UserSettingsDto(null, null, null, null));
    }

    @Override
    public UserSettingsDto updateUserSettings(UUID userId, UserSettingsDto userSettingsDto) {
        UserSettings settings = getOrCreateSettings(userId);
        settings.updateSettings(
            userSettingsDto.defaultDonationAmount(),
            userSettingsDto.defaultMaxParticipants(),
            userSettingsDto.paymentGatewayUrl(),
            userSettingsDto.showBecomeOrganizer()
        );
        UserSettings saved = userSettingsRepository.save(settings);
        return userSettingsMapper.toDto(saved);
    }

    @Override
    public UserSettingsDto updateDefaultDonationAmount(UUID userId, String amount) {
        UserSettings settings = getOrCreateSettings(userId);
        settings.updateDefaultDonationAmount(amount);
        UserSettings saved = userSettingsRepository.save(settings);
        return userSettingsMapper.toDto(saved);
    }

    @Override
    public UserSettingsDto updateDefaultMaxParticipants(UUID userId, Integer maxParticipants) {
        UserSettings settings = getOrCreateSettings(userId);
        settings.updateDefaultMaxParticipants(maxParticipants);
        UserSettings saved = userSettingsRepository.save(settings);
        return userSettingsMapper.toDto(saved);
    }

    @Override
    public UserSettingsDto updatePaymentGatewayUrl(UUID userId, String paymentGatewayUrl) {
        UserSettings settings = getOrCreateSettings(userId);
        settings.updatePaymentGatewayUrl(paymentGatewayUrl);
        UserSettings saved = userSettingsRepository.save(settings);
        return userSettingsMapper.toDto(saved);
    }

    @Override
    public UserSettingsDto updateShowBecomeOrganizer(UUID userId, Boolean showBecomeOrganizer) {
        UserSettings settings = getOrCreateSettings(userId);
        settings.updateShowBecomeOrganizer(showBecomeOrganizer);
        UserSettings saved = userSettingsRepository.save(settings);
        return userSettingsMapper.toDto(saved);
    }

    private UserSettings getOrCreateSettings(UUID userId) {
        Optional<UserSettings> settingsOpt = userSettingsRepository.findByUserId(userId);
        return settingsOpt.orElseGet(() -> {
            UserSettings newSettings = new UserSettings();
            newSettings.setUserId(userId);
            return newSettings;
        });
    }
}