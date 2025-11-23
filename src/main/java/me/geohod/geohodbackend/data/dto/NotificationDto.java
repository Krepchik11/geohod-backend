package me.geohod.geohodbackend.data.dto;

import java.time.Instant;
import java.util.UUID;

import me.geohod.geohodbackend.service.notification.NotificationType;

public record NotificationDto(
        Long id,
        UUID userId,
        NotificationType type,
        String payload,
        boolean isRead,
        Instant createdAt,
        UUID eventId
) {
} 