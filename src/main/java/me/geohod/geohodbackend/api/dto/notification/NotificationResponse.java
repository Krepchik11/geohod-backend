package me.geohod.geohodbackend.api.dto.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String payload,
        boolean isRead,
        Instant createdAt
) {
} 