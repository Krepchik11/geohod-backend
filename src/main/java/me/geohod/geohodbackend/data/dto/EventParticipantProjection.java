package me.geohod.geohodbackend.data.dto;

public record EventParticipantProjection(
        String id,
        String username,
        String tgUserId,
        String name,
        String imageUrl
) {
}
