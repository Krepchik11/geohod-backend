package me.geohod.geohodbackend.api.dto.notification;

import me.geohod.geohodbackend.service.notification.NotificationType;
import java.time.Instant;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String payload,
        boolean isRead,
        Instant createdAt
) {
} 