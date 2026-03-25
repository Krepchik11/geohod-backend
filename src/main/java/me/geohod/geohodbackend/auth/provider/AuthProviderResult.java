package me.geohod.geohodbackend.auth.provider;

public record AuthProviderResult(
        String providerId,
        AuthProviderType type,
        boolean authenticated,
        String message,
        String username,
        String firstName,
        String lastName,
        String imageUrl
) {
    public static AuthProviderResult authenticated(String providerId, AuthProviderType type) {
        return new AuthProviderResult(providerId, type, true, null, null, null, null, null);
    }

    public static AuthProviderResult authenticatedWithProfile(String providerId, AuthProviderType type,
                                                              String username, String firstName,
                                                              String lastName, String imageUrl) {
        return new AuthProviderResult(providerId, type, true, null, username, firstName, lastName, imageUrl);
    }

    public static AuthProviderResult pending(String message) {
        return new AuthProviderResult(null, null, false, message, null, null, null, null);
    }
}
