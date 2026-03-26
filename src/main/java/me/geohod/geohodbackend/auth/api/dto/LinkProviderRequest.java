package me.geohod.geohodbackend.auth.api.dto;

import me.geohod.geohodbackend.auth.provider.AuthProviderType;
import org.springframework.lang.Nullable;

public record LinkProviderRequest(
        AuthProviderType providerType,
        @Nullable String initData,
        @Nullable String email,
        @Nullable String code,
        @Nullable String oidcCode,
        @Nullable String redirectUri,
        @Nullable String codeVerifier,
        @Nullable String nonce
) {}
