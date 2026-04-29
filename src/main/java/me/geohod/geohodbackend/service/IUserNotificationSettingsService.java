package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.data.dto.NotificationSettingDto;
import me.geohod.geohodbackend.service.notification.NotificationChannel;
import me.geohod.geohodbackend.service.notification.NotificationRole;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;

import java.util.List;
import java.util.UUID;

public interface IUserNotificationSettingsService {

    List<NotificationSettingDto> getSettings(UUID userId);

    NotificationSettingDto updateChannelSetting(UUID userId, StrategyNotificationType type,
                                                NotificationRole role, NotificationChannel channel,
                                                boolean enabled);
}
