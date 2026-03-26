package me.geohod.geohodbackend.api.dto.notification;

import java.time.Instant;
import java.util.UUID;

import me.geohod.geohodbackend.service.notification.NotificationType;

public record NotificationResponse(
        Long id,
        UUID userId,
        NotificationType type,
        String payload,
        boolean isRead,
        Instant createdAt,
        UUID eventId
) {
}