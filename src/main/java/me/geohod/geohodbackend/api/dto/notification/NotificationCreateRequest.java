package me.geohod.geohodbackend.api.dto.notification;

import me.geohod.geohodbackend.service.notification.NotificationType;
import java.util.UUID;

public record NotificationCreateRequest(
        UUID userId,
        NotificationType type,
        String payload
) {
} 