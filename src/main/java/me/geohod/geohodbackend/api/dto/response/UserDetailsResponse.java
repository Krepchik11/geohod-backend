package me.geohod.geohodbackend.api.dto.response;

public record UserDetailsResponse(
    String name,
    String username,
    String imageUrl
) {
}