package me.geohod.geohodbackend.auth.api;

import me.geohod.geohodbackend.auth.api.dto.AuthTokenResponse;
import me.geohod.geohodbackend.auth.api.dto.LinkProviderRequest;
import me.geohod.geohodbackend.auth.api.dto.TelegramLoginRequest;
import me.geohod.geohodbackend.auth.api.dto.TelegramOidcLoginRequest;
import me.geohod.geohodbackend.auth.provider.AuthProviderType;
import me.geohod.geohodbackend.auth.service.AuthService;
import me.geohod.geohodbackend.security.principal.AppPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTelegramOidcTest {

    @Mock AuthService authService;
    @InjectMocks AuthController authController;

    @Test
    void telegramOidcLogin_validRequest_returnsTokens() {
        var request = new TelegramOidcLoginRequest("auth-code", "https://example.com/callback", "verifier", "nonce", null);
        when(authService.authenticate(eq(AuthProviderType.TELEGRAM), any(TelegramOidcLoginRequest.class)))
                .thenReturn(AuthTokenResponse.tokens("access-12345", "refresh-45678910"));

        var response = authController.telegramOidcLogin(request);

        assertEquals("SUCCESS", response.getResult());
        assertNotNull(response.getData());
        assertEquals("access-12345", response.getData().accessToken());
        assertEquals("refresh-45678910", response.getData().refreshToken());
        verify(authService).authenticate(eq(AuthProviderType.TELEGRAM), eq(request));
    }

    @Test
    void telegramOidcLogin_authServiceThrowsSecurity_propagatesException() {
        var request = new TelegramOidcLoginRequest("bad-code", "https://example.com/callback", null, null, null);
        when(authService.authenticate(eq(AuthProviderType.TELEGRAM), any()))
                .thenThrow(new SecurityException("OIDC verification failed"));

        assertThrows(SecurityException.class, () -> authController.telegramOidcLogin(request));
    }

    @Test
    void linkProvider_withOidcCode_constructsTelegramOidcLoginRequest() {
        var principal = new AppPrincipal(UUID.randomUUID(), List.of("USER"));
        var request = new LinkProviderRequest(
                AuthProviderType.TELEGRAM, null, null, null,
                "oidc-auth-code", "https://example.com/callback", "verifier", "nonce");

        authController.linkProvider(principal, request);

        verify(authService).linkProvider(eq(principal.userId()), eq(AuthProviderType.TELEGRAM),
                argThat(arg -> arg instanceof TelegramOidcLoginRequest oidc
                        && "oidc-auth-code".equals(oidc.code())
                        && "https://example.com/callback".equals(oidc.redirectUri())));
    }

    @Test
    void linkProvider_withoutOidcCode_constructsTelegramLoginRequest() {
        var principal = new AppPrincipal(UUID.randomUUID(), List.of("USER"));
        var request = new LinkProviderRequest(
                AuthProviderType.TELEGRAM, "init-data", null, null,
                null, null, null, null);

        authController.linkProvider(principal, request);

        verify(authService).linkProvider(eq(principal.userId()), eq(AuthProviderType.TELEGRAM),
                argThat(arg -> arg instanceof TelegramLoginRequest(String initData)
                        && "init-data".equals(initData)));
    }
}
