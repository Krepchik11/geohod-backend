package me.geohod.geohodbackend.service;

import java.util.UUID;

public interface INotificationService {
    void sendNotification(UUID userId, String message);
}
