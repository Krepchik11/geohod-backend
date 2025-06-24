package me.geohod.geohodbackend.api.dto.notification;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.Instant;

public record NotificationCursorRequest(
        @Min(1)
        @Max(100)
        Integer limit,
        Boolean isRead,
        Instant cursor
) {
    public NotificationCursorRequest {
        if (limit == null) {
            limit = 20;
        }
        if (isRead == null) {
            isRead = false;
        }
    }
} 