package me.geohod.geohodbackend.auth.provider;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.auth.api.dto.TelegramLoginRequest;
import me.geohod.geohodbackend.auth.api.dto.TelegramOidcLoginRequest;
import me.geohod.geohodbackend.auth.telegram.OidcUserInfo;
import me.geohod.geohodbackend.auth.telegram.TelegramInitDataVerifier;
import me.geohod.geohodbackend.auth.telegram.TelegramOidcClient;
import me.geohod.geohodbackend.auth.telegram.VerifiedInitData;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelegramAuthProvider implements AuthProvider {
    private final TelegramInitDataVerifier telegramInitDataVerifier;
    private final TelegramOidcClient telegramOidcClient;

    @Override
    public AuthProviderType getType() {
        return AuthProviderType.TELEGRAM;
    }

    @Override
    public AuthProviderResult authenticate(Object request) {
        if (request instanceof TelegramLoginRequest r) {
            VerifiedInitData data = telegramInitDataVerifier.verifyAndExtract(r.initData());
            return AuthProviderResult.authenticatedWithProfile(
                    data.telegramUserId(), AuthProviderType.TELEGRAM,
                    data.username(), data.firstName(),
                    data.lastName(), data.photoUrl());
        }
        if (request instanceof TelegramOidcLoginRequest r) {
            OidcUserInfo data = telegramOidcClient.exchangeAndVerify(
                    r.code(), r.redirectUri(), r.codeVerifier(), r.nonce());
            return AuthProviderResult.authenticatedWithProfile(
                    data.telegramUserId(), AuthProviderType.TELEGRAM,
                    data.username(), data.name(),
                    null, data.photoUrl());
        }
        throw new IllegalArgumentException("Unsupported request type for Telegram provider");
    }

    @Override
    public boolean supports(AuthProviderType type) {
        return AuthProviderType.TELEGRAM.equals(type);
    }
}
