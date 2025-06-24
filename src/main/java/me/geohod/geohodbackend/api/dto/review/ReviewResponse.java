package me.geohod.geohodbackend.api.dto.review;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID eventId,
        UUID authorId,
        UUID targetUserId,
        int rating,
        String comment,
        Instant createdAt
) {
} 