package me.geohod.geohodbackend.data.dto;

import me.geohod.geohodbackend.data.model.Event;

import java.time.Instant;
import java.util.UUID;

public record EventDto(
        UUID id,
        UUID authorId,
        String name,
        String description,
        Instant date,
        int maxParticipants,
        int currentParticipants,
        Event.Status status
) {
}
