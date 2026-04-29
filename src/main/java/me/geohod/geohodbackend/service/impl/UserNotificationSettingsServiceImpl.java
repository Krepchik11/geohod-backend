package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.dto.NotificationSettingDto;
import me.geohod.geohodbackend.data.model.UserNotificationSetting;
import me.geohod.geohodbackend.data.model.repository.UserNotificationSettingRepository;
import me.geohod.geohodbackend.service.IUserNotificationSettingsService;
import me.geohod.geohodbackend.service.notification.NotificationChannel;
import me.geohod.geohodbackend.service.notification.NotificationRole;
import me.geohod.geohodbackend.service.notification.NotificationSettingsDefaults;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserNotificationSettingsServiceImpl implements IUserNotificationSettingsService {

    private final UserNotificationSettingRepository repository;
    private final NotificationSettingsDefaults defaults;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationSettingDto> getSettings(UUID userId) {
        Map<String, UserNotificationSetting> stored = repository.findAllByUserId(userId).stream()
                .collect(Collectors.toMap(s -> pairKey(s.getType(), s.getRole()), Function.identity()));

        List<NotificationSettingDto> result = new ArrayList<>();
        for (StrategyNotificationType type : StrategyNotificationType.values()) {
            for (NotificationRole role : NotificationRole.values()) {
                Map<NotificationChannel, Boolean> channels = buildChannels(type, role, stored.get(pairKey(type, role)));
                if (!channels.isEmpty()) {
                    result.add(new NotificationSettingDto(type, role, channels));
                }
            }
        }
        return result;
    }

    @Override
    public NotificationSettingDto updateChannelSetting(UUID userId, StrategyNotificationType type,
                                                        NotificationRole role, NotificationChannel channel,
                                                        boolean enabled) {
        if (!defaults.isValid(type, role, channel)) {
            throw new IllegalArgumentException(
                    "Invalid notification combination: " + type + ":" + role + ":" + channel);
        }
        UserNotificationSetting setting = repository.findByUserIdAndTypeAndRole(userId, type, role)
                .orElseGet(() -> createWithDefaults(userId, type, role));
        setting.updateChannel(channel, enabled);
        UserNotificationSetting saved = repository.save(setting);
        return new NotificationSettingDto(type, role, buildChannels(type, role, saved));
    }

    private Map<NotificationChannel, Boolean> buildChannels(StrategyNotificationType type, NotificationRole role,
                                                              UserNotificationSetting stored) {
        Map<NotificationChannel, Boolean> channels = new LinkedHashMap<>();
        for (NotificationChannel channel : NotificationChannel.values()) {
            if (defaults.isValid(type, role, channel)) {
                boolean enabled = stored != null
                        ? channelValue(stored, channel)
                        : defaults.getDefaultEnabled(type, role, channel);
                channels.put(channel, enabled);
            }
        }
        return channels;
    }

    private boolean channelValue(UserNotificationSetting s, NotificationChannel channel) {
        return switch (channel) {
            case TELEGRAM -> s.isTelegram();
            case IN_APP -> s.isInApp();
            case PUSH -> s.isPush();
        };
    }

    private UserNotificationSetting createWithDefaults(UUID userId, StrategyNotificationType type, NotificationRole role) {
        return new UserNotificationSetting(userId, type, role,
                defaults.getDefaultEnabled(type, role, NotificationChannel.TELEGRAM),
                defaults.getDefaultEnabled(type, role, NotificationChannel.IN_APP),
                defaults.getDefaultEnabled(type, role, NotificationChannel.PUSH));
    }

    private String pairKey(StrategyNotificationType type, NotificationRole role) {
        return type.name() + ":" + role.name();
    }
}
