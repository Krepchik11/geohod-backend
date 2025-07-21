package me.geohod.geohodbackend.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("geohod")
public record GeohodProperties(
        TelegramBot telegramBot,
        LinkTemplates linkTemplates
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
}
