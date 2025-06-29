package me.geohod.geohodbackend.data.dto;

import me.geohod.geohodbackend.service.notification.NotificationType;
import java.time.Instant;

public record NotificationDto(
        Long id,
        NotificationType type,
        String payload,
        boolean isRead,
        Instant createdAt
) {
} 