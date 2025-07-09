package me.geohod.geohodbackend.api.dto.review;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID eventId,
        UUID authorId,
        String authorUsername,
        String authorImageUrl,
        int rating,
        String comment,
        Boolean isHidden,
        Instant createdAt
) {
} 