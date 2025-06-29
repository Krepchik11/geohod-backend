package me.geohod.geohodbackend.data.dto;

import me.geohod.geohodbackend.service.notification.NotificationType;
import java.util.UUID;

public record NotificationCreateDto(
        UUID userId,
        NotificationType type,
        String payload
) {
} 