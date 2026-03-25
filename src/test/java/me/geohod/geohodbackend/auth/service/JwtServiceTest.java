package me.geohod.geohodbackend.auth.service;

import io.jsonwebtoken.Claims;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        var jwtProps = new GeohodProperties.Jwt(
                "test-secret-key-that-is-at-least-32-characters-long-for-hmac-sha256",
                Duration.ofMinutes(15),
                Duration.ofDays(30)
        );
        jwtService = new JwtService(jwtProps);
    }

    @Test
    void generateAndValidateAccessToken() {
        UUID userId = UUID.randomUUID();
        List<String> roles = List.of("USER");

        String token = jwtService.generateAccessToken(userId, roles);

        assertNotNull(token);
        Claims claims = jwtService.validateAccessToken(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(roles, claims.get("roles", List.class));
    }

    @Test
    void validateAccessToken_expired_throwsException() {
        var shortLivedProps = new GeohodProperties.Jwt(
                "test-secret-key-that-is-at-least-32-characters-long-for-hmac-sha256",
                Duration.ofMillis(1),
                Duration.ofDays(30)
        );
        var shortLivedJwtService = new JwtService(shortLivedProps);
        UUID userId = UUID.randomUUID();
        String token = shortLivedJwtService.generateAccessToken(userId, List.of("USER"));

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThrows(Exception.class, () -> shortLivedJwtService.validateAccessToken(token));
    }

    @Test
    void validateAccessToken_invalidToken_throwsException() {
        assertThrows(Exception.class, () -> jwtService.validateAccessToken("invalid.token.here"));
    }

    @Test
    void generateRefreshToken_returnsNonEmptyString() {
        String refreshToken = jwtService.generateRefreshToken();
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }
}
