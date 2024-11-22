package me.geohod.geohodbackend.data.dto;

import me.geohod.geohodbackend.data.model.Event;

import java.time.Instant;
import java.util.UUID;

public record EventDetailedProjection(
        UUID id,
        TelegramUserDetails author,
        String name,
        String description,
        Instant date,
        int maxParticipants,
        int currentParticipants,
        Event.Status status
) {
}
