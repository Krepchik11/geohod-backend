package me.geohod.geohodbackend.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("geohod")
public record GeohodProperties(
        TelegramBot telegramBot
) {
    public record TelegramBot(
            String token
    ) {
    }
}
