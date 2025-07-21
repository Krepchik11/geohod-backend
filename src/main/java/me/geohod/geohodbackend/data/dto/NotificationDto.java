package me.geohod.geohodbackend.data.dto;

import me.geohod.geohodbackend.service.notification.NotificationType;
import java.time.Instant;
import java.util.UUID;

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