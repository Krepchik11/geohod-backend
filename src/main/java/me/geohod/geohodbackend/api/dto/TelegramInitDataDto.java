package me.geohod.geohodbackend.api.dto;

public record TelegramInitDataDto(
        String id,
        String firstName,
        String lastName,
        String username,
        String languageCode,
        String photoUrl
) {
}
