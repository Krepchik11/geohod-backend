package me.geohod.geohodbackend.data.dto;

import java.util.UUID;

public record CancelEventDto(
    UUID eventId,
    boolean notifyParticipants
) {
}
