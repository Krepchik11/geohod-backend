package me.geohod.geohodbackend.api.dto.response;

import java.util.UUID;

public record UserDetailsResponse(
    UUID id,
    String name,
    String username,
    String imageUrl
) {
}