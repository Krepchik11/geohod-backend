package me.geohod.geohodbackend.auth.telegram;

public record OidcUserInfo(
        String telegramUserId,
        String name,
        String username,
        String photoUrl
) {}
