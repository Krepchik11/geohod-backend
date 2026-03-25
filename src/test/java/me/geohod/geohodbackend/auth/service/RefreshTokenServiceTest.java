package me.geohod.geohodbackend.auth.service;

import me.geohod.geohodbackend.auth.data.model.RefreshToken;
import me.geohod.geohodbackend.auth.data.repository.RefreshTokenRepository;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    @Mock RefreshTokenRepository refreshTokenRepository;
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        var jwtProps = new GeohodProperties.Jwt(
                "test-secret-key-that-is-at-least-32-characters-long-for-hmac-sha256",
                Duration.ofMinutes(15),
                Duration.ofDays(30)
        );
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, jwtProps);
    }

    @Test
    void create_savesHashedTokenAndReturnsRawToken() {
        UUID userId = UUID.randomUUID();
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String rawToken = refreshTokenService.create(userId);

        assertNotNull(rawToken);
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertNotEquals(rawToken, saved.getTokenHash()); // hash != raw
    }

    @Test
    void validate_validToken_returnsUserId() {
        UUID userId = UUID.randomUUID();
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        String rawToken = refreshTokenService.create(userId);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();

        when(refreshTokenRepository.findByTokenHash(saved.getTokenHash()))
                .thenReturn(Optional.of(saved));

        UUID result = refreshTokenService.validate(rawToken);
        assertEquals(userId, result);
    }

    @Test
    void validate_expiredToken_throws() {
        RefreshToken expired = new RefreshToken(UUID.randomUUID(), "hash", Instant.now().minusSeconds(60));
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(expired));

        assertThrows(SecurityException.class, () -> refreshTokenService.validate("any-token"));
    }

    @Test
    void revoke_deletesToken() {
        RefreshToken token = new RefreshToken(UUID.randomUUID(), "hash", Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        refreshTokenService.revoke("any-token");

        verify(refreshTokenRepository).delete(token);
    }
}
