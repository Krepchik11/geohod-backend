package me.geohod.geohodbackend.service;

import java.util.UUID;

public interface ITelegramNotificationService {
    void sendNotification(UUID userId, String message);
}