package me.geohod.geohodbackend.api.dto.notification;

import me.geohod.geohodbackend.service.notification.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String payload,
        boolean isRead,
        Instant createdAt
) {
} 