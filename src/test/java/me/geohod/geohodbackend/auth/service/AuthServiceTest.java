package me.geohod.geohodbackend.auth.service;

import me.geohod.geohodbackend.auth.api.dto.AuthTokenResponse;
import me.geohod.geohodbackend.auth.api.dto.TelegramLoginRequest;
import me.geohod.geohodbackend.auth.data.model.AuthProviderEntity;
import me.geohod.geohodbackend.auth.data.repository.AuthProviderRepository;
import me.geohod.geohodbackend.auth.data.repository.UserRoleRepository;
import me.geohod.geohodbackend.auth.provider.AuthProvider;
import me.geohod.geohodbackend.auth.provider.AuthProviderResult;
import me.geohod.geohodbackend.auth.provider.AuthProviderType;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock AuthProviderRepository authProviderRepository;
    @Mock UserRoleRepository userRoleRepository;
    @Mock JwtService jwtService;
    @Mock RefreshTokenService refreshTokenService;
    @Mock IUserService userService;
    @Mock AuthProvider telegramProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                List.of(telegramProvider),
                authProviderRepository, userRoleRepository,
                jwtService, refreshTokenService, userService
        );
    }

    @Test
    void authenticate_existingUser_returnsTokens() {
        TelegramLoginRequest request = new TelegramLoginRequest("valid-data");
        UUID userId = UUID.randomUUID();

        when(telegramProvider.supports(AuthProviderType.TELEGRAM)).thenReturn(true);
        when(telegramProvider.authenticate(request))
                .thenReturn(AuthProviderResult.authenticatedWithProfile("tg123", AuthProviderType.TELEGRAM,
                        "johndoe", "John", "Doe", null));

        AuthProviderEntity providerEntity = new AuthProviderEntity(userId, AuthProviderType.TELEGRAM, "tg123");
        when(authProviderRepository.findByProviderTypeAndProviderId(AuthProviderType.TELEGRAM, "tg123"))
                .thenReturn(Optional.of(providerEntity));

        when(userRoleRepository.findRolesByUserId(userId)).thenReturn(List.of("USER"));
        when(jwtService.generateAccessToken(userId, List.of("USER"))).thenReturn("access-token");
        when(refreshTokenService.create(userId)).thenReturn("refresh-token");

        AuthTokenResponse response = authService.authenticate(AuthProviderType.TELEGRAM, request);

        assertTrue(response.authenticated());
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
    }

    @Test
    void authenticate_newUser_createsUserAndReturnsTokens() {
        TelegramLoginRequest request = new TelegramLoginRequest("valid-data");
        UUID userId = UUID.randomUUID();
        User newUser = mock(User.class);
        when(newUser.getId()).thenReturn(userId);

        when(telegramProvider.supports(AuthProviderType.TELEGRAM)).thenReturn(true);
        when(telegramProvider.authenticate(request))
                .thenReturn(AuthProviderResult.authenticatedWithProfile("tg123", AuthProviderType.TELEGRAM,
                        "johndoe", "John", "Doe", null));

        when(authProviderRepository.findByProviderTypeAndProviderId(AuthProviderType.TELEGRAM, "tg123"))
                .thenReturn(Optional.empty());
        when(userService.createOrUpdateUser(eq("tg123"), eq("johndoe"), eq("John"), eq("Doe"), any())).thenReturn(newUser);
        when(authProviderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRoleRepository.findRolesByUserId(userId)).thenReturn(List.of("USER"));
        when(jwtService.generateAccessToken(userId, List.of("USER"))).thenReturn("access-token");
        when(refreshTokenService.create(userId)).thenReturn("refresh-token");

        AuthTokenResponse response = authService.authenticate(AuthProviderType.TELEGRAM, request);

        assertTrue(response.authenticated());
        verify(authProviderRepository).save(any(AuthProviderEntity.class));
    }

    @Test
    void authenticate_pendingResult_returnsPendingResponse() {
        var emailProvider = mock(AuthProvider.class);
        when(emailProvider.supports(AuthProviderType.EMAIL)).thenReturn(true);
        when(emailProvider.authenticate(any()))
                .thenReturn(AuthProviderResult.pending("OTP sent"));

        var service = new AuthService(
                List.of(emailProvider),
                authProviderRepository, userRoleRepository,
                jwtService, refreshTokenService, userService
        );

        AuthTokenResponse response = service.authenticate(AuthProviderType.EMAIL, new Object());

        assertFalse(response.authenticated());
        assertEquals("OTP sent", response.message());
        assertNull(response.accessToken());
    }

    @Test
    void refresh_validToken_returnsNewTokens() {
        UUID userId = UUID.randomUUID();
        when(refreshTokenService.validate("old-refresh")).thenReturn(userId);
        when(userRoleRepository.findRolesByUserId(userId)).thenReturn(List.of("USER"));
        when(jwtService.generateAccessToken(userId, List.of("USER"))).thenReturn("new-access");
        when(refreshTokenService.create(userId)).thenReturn("new-refresh");

        AuthTokenResponse response = authService.refresh("old-refresh");

        assertTrue(response.authenticated());
        assertEquals("new-access", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
        verify(refreshTokenService).revoke("old-refresh");
    }
}
