package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.data.model.notification.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface IAppNotificationService {
    List<Notification> getNotifications(UUID userId, Integer limit, Boolean isRead, Instant cursorCreatedAt);
    void markAsRead(UUID notificationId, UUID userId);
    void markAllAsRead(UUID userId);
    Notification createNotification(Notification notification);
}
