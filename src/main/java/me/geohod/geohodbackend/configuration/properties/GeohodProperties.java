package me.geohod.geohodbackend.configuration.properties;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("geohod")
public record GeohodProperties(
                TelegramBot telegramBot,
                LinkTemplates linkTemplates,
                Cors cors,
                Security security) {
        public record TelegramBot(
                        String token,
                        String username) {
        }

        public record LinkTemplates(
                        String startappLink) {
        }

        public record Cors(
                        List<String> allowedOrigins,
                        List<String> allowedMethods,
                        List<String> allowedHeaders,
                        Boolean allowCredentials,
                        Long maxAge) {
        }

        public record Security(
                        Jwt jwt,
                        LegacyTelegramAuth legacyTelegramAuth,
                        EmailOtp emailOtp,
                        TelegramOidc telegramOidc,
                        TelegramInitData telegramInitData) {
        }

        public record Jwt(
                        String secret,
                        Duration accessTokenExpiration,
                        Duration refreshTokenExpiration) {
        }

        public record LegacyTelegramAuth(boolean enabled) {
        }

        public record EmailOtp(
                        int codeLength,
                        Duration expiration,
                        int maxAttempts,
                        int maxSendsPerHour) {
        }

        public record TelegramOidc(
                        String clientId,
                        String clientSecret,
                        String issuerUri,
                        String jwksUri,
                        String tokenUri,
                        Duration jwksCacheTtl) {
        }

        public record TelegramInitData(Duration maxAge) {
        }
}
