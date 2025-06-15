package me.geohod.geohodbackend.api.dto.response;

import me.geohod.geohodbackend.data.model.Event;

import java.time.Instant;

public record EventDetailsResponse(
        String id,
        TelegramUserDetails author,
        String name,
        String description,
        Instant date,
        int maxParticipants,
        int currentParticipants,
        Event.Status status
) {
}