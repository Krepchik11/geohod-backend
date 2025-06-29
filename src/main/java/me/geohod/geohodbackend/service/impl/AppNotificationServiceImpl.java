package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.notification.Notification;
import me.geohod.geohodbackend.data.model.repository.NotificationRepository;
import me.geohod.geohodbackend.service.IAppNotificationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppNotificationServiceImpl implements IAppNotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<Notification> getNotifications(UUID userId, Integer limit, Boolean isRead, Long cursorAfterId) {
        if (limit == null || limit < 1 || limit > 100) {
            limit = 20;
        }
        if (isRead == null) {
            isRead = false;
        }
        if (cursorAfterId == null) {
            // Fetch first page
            return notificationRepository.findByUserIdAndIsReadOrderByIdDesc(userId, isRead, org.springframework.data.domain.PageRequest.of(0, limit));
        } else {
            return notificationRepository.findByUserIdAndIsReadAfterCursor(userId, isRead, cursorAfterId, limit);
        }
    }

    @Override
    public void dismiss(Long notificationId, UUID userId) {
        if (notificationId == null) {
            throw new IllegalArgumentException("Notification ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

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