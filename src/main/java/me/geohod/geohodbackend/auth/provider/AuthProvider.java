package me.geohod.geohodbackend.auth.provider;

public interface AuthProvider {
    AuthProviderType getType();
    AuthProviderResult authenticate(Object request);
    boolean supports(AuthProviderType type);
}
