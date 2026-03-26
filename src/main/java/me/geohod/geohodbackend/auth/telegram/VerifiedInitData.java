package me.geohod.geohodbackend.auth.telegram;

public record VerifiedInitData(
        String telegramUserId,
        String username,
        String firstName,
        String lastName,
        String photoUrl
) {
}
