package me.geohod.geohodbackend.service.notification;

import java.util.UUID;

public interface ITelegramNotificationService {
    void sendNotification(UUID userId, String message);
}