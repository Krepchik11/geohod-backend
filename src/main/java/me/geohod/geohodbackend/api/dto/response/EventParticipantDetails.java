package me.geohod.geohodbackend.api.dto.response;

import java.util.UUID;

public record EventParticipantDetails(
        UUID id,
        String username,
        String name,
        String imageUrl
) {
}
