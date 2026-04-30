package me.geohod.geohodbackend.api.dto.response;

import java.util.UUID;

public record CurrentUserDetailsResponse(
    UUID id,
    String tgId,
    String name,
    String username,
    String imageUrl
) {
}
