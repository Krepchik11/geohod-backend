package me.geohod.geohodbackend.data.dto;

import java.util.UUID;

public record EventParticipantDto(
        UUID eventId,
        UUID userId
) {
}
