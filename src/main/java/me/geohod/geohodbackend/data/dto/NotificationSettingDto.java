package me.geohod.geohodbackend.data.dto;

import me.geohod.geohodbackend.service.notification.NotificationChannel;
import me.geohod.geohodbackend.service.notification.NotificationRole;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;

import java.util.Map;

public record NotificationSettingDto(
        StrategyNotificationType type,
        NotificationRole role,
        Map<NotificationChannel, Boolean> channels
) {}
