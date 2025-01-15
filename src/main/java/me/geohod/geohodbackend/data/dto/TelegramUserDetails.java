package me.geohod.geohodbackend.data.dto;

public record TelegramUserDetails(
        String username,
        String firstName,
        String lastName,
        String imageUrl
) {
}