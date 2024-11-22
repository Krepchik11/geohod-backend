package me.geohod.geohodbackend.api.dto.response;

import me.geohod.geohodbackend.data.model.Event;

import java.time.Instant;

public record EventResponse(
        String id,
        String name,
        String description,
        Instant date,
        int currentParticipants,
        Event.Status status
) {
}
