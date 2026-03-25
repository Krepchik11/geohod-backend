package me.geohod.geohodbackend.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final GeohodProperties.Jwt jwtProperties;

    JwtService(GeohodProperties.Jwt jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Autowired
    public JwtService(GeohodProperties geohodProperties) {
        this(geohodProperties.security().jwt());
    }

    public String generateAccessToken(UUID userId, List<String> roles) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.accessTokenExpiration().toMillis());

        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public Claims validateAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }
}
