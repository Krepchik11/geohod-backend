package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.notification.NotificationCursorRequest;
import me.geohod.geohodbackend.data.model.notification.Notification;
import me.geohod.geohodbackend.data.model.repository.NotificationRepository;
import me.geohod.geohodbackend.service.IAppNotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements IAppNotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<Notification> getNotifications(UUID userId, NotificationCursorRequest cursorRequest) {
        PageRequest pageRequest = PageRequest.of(0, cursorRequest.limit());
        // Cursor logic will be added here later. For now, just fetch the first page.
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, cursorRequest.isRead(), pageRequest);
    }

    @Override
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        if (!notification.getUserId().equals(userId)) {
            throw new SecurityException("User not authorized to mark this notification as read.");
        }
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(UUID userId) {
        // This will be implemented with a custom query in the repository later.
    }
} 