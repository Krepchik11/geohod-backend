package me.geohod.geohodbackend.auth.telegram;

import com.nimbusds.jwt.JWTClaimsSet;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TelegramOidcClientTest {

    private static final String CLIENT_ID = "1234567";
    private static final String CLIENT_SECRET = "test-secret";
    private static final String ISSUER_URI = "https://oauth.telegram.org";

    private TelegramOidcClient oidcClient;

    @BeforeEach
    void setUp() {
        var oidcConfig = new GeohodProperties.TelegramOidc(
                CLIENT_ID, CLIENT_SECRET, ISSUER_URI,
                "https://oauth.telegram.org/.well-known/jwks.json",
                "https://oauth.telegram.org/token",
                Duration.ofHours(1));
        var security = new GeohodProperties.Security(null, null, null, oidcConfig, null, null);
        var properties = new GeohodProperties(null, null, null, security);

        // NOTE: We don't call init() because it tries to connect to the real JWKS URL
        // We test validateNonce and extractUserInfo which don't need the JWT processor
        oidcClient = new TelegramOidcClient(properties, null);
    }

    @Test
    void extractUserInfo_validClaims_returnsOidcUserInfo() {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(ISSUER_URI)
                .audience(CLIENT_ID)
                .subject("99999")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .claim("name", "John Doe")
                .claim("preferred_username", "johndoe")
                .claim("picture", "https://photo.url")
                .build();

        OidcUserInfo result = oidcClient.extractUserInfo(claims);

        assertEquals("99999", result.telegramUserId());
        assertEquals("John Doe", result.name());
        assertEquals("johndoe", result.username());
        assertEquals("https://photo.url", result.photoUrl());
    }

    @Test
    void validateNonce_matchingNonce_doesNotThrow() {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("nonce", "my-nonce-123")
                .build();

        assertDoesNotThrow(() -> oidcClient.validateNonce(claims, "my-nonce-123"));
    }

    @Test
    void validateNonce_mismatchedNonce_throwsSecurityException() {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("nonce", "actual-nonce")
                .build();

        SecurityException ex = assertThrows(SecurityException.class,
                () -> oidcClient.validateNonce(claims, "expected-nonce"));
        assertTrue(ex.getMessage().toLowerCase().contains("nonce"));
    }

    @Test
    void validateNonce_nullExpectedNonce_skipsValidation() {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("nonce", "any-nonce")
                .build();

        assertDoesNotThrow(() -> oidcClient.validateNonce(claims, null));
    }

    @Test
    void verifyDirectIdToken_invalidToken_throwsSecurityException() {
        // jwtProcessor is null (init() not called) — verifyIdToken wraps any exception as SecurityException
        assertThrows(SecurityException.class,
                () -> oidcClient.verifyDirectIdToken("not.a.valid.jwt", null));
    }

    @Test
    void extractUserInfo_missingOptionalFields_returnsNulls() {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("99999")
                .build();

        OidcUserInfo result = oidcClient.extractUserInfo(claims);

        assertEquals("99999", result.telegramUserId());
        assertNull(result.name());
        assertNull(result.username());
        assertNull(result.photoUrl());
    }
}
