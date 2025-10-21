package me.geohod.geohodbackend.configuration.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("geohod")
public record GeohodProperties(
        TelegramBot telegramBot,
        LinkTemplates linkTemplates,
        Cors cors
) {
    public record TelegramBot(
            String token,
            String username
    ) {
    }

    public record LinkTemplates(
            String eventRegistrationLink,
            String reviewLink
    ) {
    }

    public record Cors(
            List<String> allowedOrigins,
            List<String> allowedMethods,
            List<String> allowedHeaders,
            Boolean allowCredentials,
            Long maxAge
    ) {
    }
}
