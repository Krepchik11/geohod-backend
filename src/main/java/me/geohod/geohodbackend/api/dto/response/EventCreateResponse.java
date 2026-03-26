package me.geohod.geohodbackend.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record EventCreateResponse(
        String id,
        UUID authorId,
        String name,
        String description,
        Instant date,
        int maxParticipants,
        int currentParticipants,
        String status,
        String message
) {
}
