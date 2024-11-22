package me.geohod.geohodbackend.data.dto;

public record TelegramUserDetails(
        String username,
        String name,
        String imageUrl
) {
}