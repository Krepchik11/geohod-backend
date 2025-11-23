package me.geohod.geohodbackend.data.dto;

import java.util.UUID;

import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;

public record NotificationCreateDto(
        UUID userId,
        StrategyNotificationType type,
        String payload,
        UUID eventId
) {
}