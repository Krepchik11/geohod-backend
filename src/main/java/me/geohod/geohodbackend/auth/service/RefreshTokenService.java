package me.geohod.geohodbackend.auth.service;

import me.geohod.geohodbackend.auth.data.model.RefreshToken;
import me.geohod.geohodbackend.auth.data.repository.RefreshTokenRepository;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final GeohodProperties.Jwt jwtProperties;

    @Autowired
    public RefreshTokenService(GeohodProperties geohodProperties,
                               RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = geohodProperties.security().jwt();
    }

    RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                        GeohodProperties.Jwt jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    public String create(UUID userId) {
        String rawToken = UUID.randomUUID().toString();
        String hash = hashToken(rawToken);
        Instant expiresAt = Instant.now().plus(jwtProperties.refreshTokenExpiration());

        RefreshToken refreshToken = new RefreshToken(userId, hash, expiresAt);
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    public UUID validate(String rawToken) {
        String hash = hashToken(rawToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new SecurityException("Invalid refresh token"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new SecurityException("Refresh token expired");
        }

        return token.getUserId();
    }

    public void revoke(String rawToken) {
        String hash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHash(hash)
                .ifPresent(refreshTokenRepository::delete);
    }

    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
