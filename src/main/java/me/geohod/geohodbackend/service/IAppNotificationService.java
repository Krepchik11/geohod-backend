package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.data.dto.NotificationCreateDto;
import me.geohod.geohodbackend.data.dto.NotificationDto;
import java.util.List;
import java.util.UUID;

public interface IAppNotificationService {
    List<NotificationDto> getNotifications(UUID userId, Integer limit, Boolean isRead, Long cursorAfterId);
    void dismiss(Long notificationId, UUID userId);
    void dismissAll(UUID userId);
    NotificationDto createNotification(NotificationCreateDto request);
}
