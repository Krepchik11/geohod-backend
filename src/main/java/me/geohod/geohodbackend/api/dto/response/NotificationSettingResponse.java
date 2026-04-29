package me.geohod.geohodbackend.api.dto.response;

import me.geohod.geohodbackend.service.notification.NotificationChannel;
import me.geohod.geohodbackend.service.notification.NotificationRole;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;

import java.util.Map;

public record NotificationSettingResponse(
        StrategyNotificationType eventType,
        NotificationRole role,
        Map<NotificationChannel, Boolean> channels
) {}
