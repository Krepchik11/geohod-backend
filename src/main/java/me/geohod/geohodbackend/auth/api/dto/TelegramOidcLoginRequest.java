package me.geohod.geohodbackend.auth.api.dto;

import org.springframework.lang.Nullable;

public record TelegramOidcLoginRequest(
        String code,
        String redirectUri,
        @Nullable String codeVerifier,
        @Nullable String nonce
) {}
