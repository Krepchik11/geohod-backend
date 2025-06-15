package me.geohod.geohodbackend.api.dto.response;

public record TelegramUserDetails(
        String id,
        String username,
        String name,
        String imageUrl
) {
}