package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.notification.Notification;
import me.geohod.geohodbackend.data.model.repository.NotificationRepository;
import me.geohod.geohodbackend.service.IAppNotificationService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppNotificationServiceImpl implements IAppNotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<Notification> getNotifications(UUID userId, Integer limit, Boolean isRead, Instant cursorCreatedAt) {
        if (limit == null || limit < 1 || limit > 100) {
            limit = 20;
        }
        if (isRead == null) {
            isRead = false;
        }
        if (cursorCreatedAt == null) {
            // Fetch first page
            return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead, org.springframework.data.domain.PageRequest.of(0, limit));
        } else {
            return notificationRepository.findByUserIdAndIsReadBeforeCursor(userId, isRead, cursorCreatedAt, limit);
        }
    }

    @Override
    public void dismiss(UUID notificationId, UUID userId) {
        if (notificationId == null) {
            throw new IllegalArgumentException("Notification ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Notification does not belong to the specified user");
        }
        notification.dismiss();
        notificationRepository.save(notification);
    }

    @Override
    public void dismissAll(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        notificationRepository.dismissAllByUser(userId);
    }

    @Override
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }
} 