package me.geohod.geohodbackend.service;

import java.util.UUID;

public interface ITelegramOutboxMessagePublisher {
    void publish(UUID userId, String message);
}
