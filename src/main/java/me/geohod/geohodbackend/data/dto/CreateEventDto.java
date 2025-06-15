package me.geohod.geohodbackend.data.dto;

import java.time.Instant;
import java.util.UUID;

public record CreateEventDto(
        UUID authorId, String name, String description, Instant date, int maxParticipants
) {
}
