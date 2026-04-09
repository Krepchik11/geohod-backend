package me.geohod.geohodbackend.auth.api.dto;

import org.springframework.lang.Nullable;

public record TelegramOidcLoginRequest(
        @Nullable String code,
        @Nullable String redirectUri,
        @Nullable String codeVerifier,
        @Nullable String nonce,
        @Nullable String idToken
) {}
