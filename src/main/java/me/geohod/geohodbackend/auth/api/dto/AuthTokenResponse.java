package me.geohod.geohodbackend.auth.api.dto;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        boolean authenticated,
        String message
) {
    public static AuthTokenResponse tokens(String accessToken, String refreshToken) {
        return new AuthTokenResponse(accessToken, refreshToken, true, null);
    }

    public static AuthTokenResponse pending(String message) {
        return new AuthTokenResponse(null, null, false, message);
    }
}
