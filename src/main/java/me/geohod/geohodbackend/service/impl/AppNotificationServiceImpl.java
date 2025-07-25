package me.geohod.geohodbackend.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.dto.NotificationCreateDto;
import me.geohod.geohodbackend.data.dto.NotificationDto;
import me.geohod.geohodbackend.data.mapper.NotificationMapper;
import me.geohod.geohodbackend.data.model.notification.Notification;
import me.geohod.geohodbackend.data.model.repository.NotificationRepository;
import me.geohod.geohodbackend.service.IAppNotificationService;

@Service
@RequiredArgsConstructor
public class AppNotificationServiceImpl implements IAppNotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public List<NotificationDto> getNotifications(UUID userId, Integer limit, Boolean isRead, Long cursorAfterId) {
        if (limit == null || limit < 1 || limit > 100) {
            limit = 20;
        }
        List<Notification> notifications;
        if (cursorAfterId == null) {
            // Fetch first page
            if (isRead == null) {
                notifications = notificationRepository.findByUserIdOrderByIdDesc(userId, PageRequest.of(0, limit));
            } else {
                notifications = notificationRepository.findByUserIdAndIsReadOrderByIdDesc(userId, isRead, PageRequest.of(0, limit));
            }
        } else {
            if (isRead == null) {
                notifications = notificationRepository.findByUserIdAfterCursor(userId, cursorAfterId, limit);
            } else {
                notifications = notificationRepository.findByUserIdAndIsReadAfterCursor(userId, isRead, cursorAfterId, limit);
            }
        }
        return notifications.stream().map(notificationMapper::toDto).toList();
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
    public NotificationDto createNotification(NotificationCreateDto request) {
        Notification notification = new Notification(
            request.eventId(),
            request.userId(),
            request.type(),
            new me.geohod.geohodbackend.data.model.eventlog.JsonbString(request.payload())
        );
        Notification savedNotification = notificationRepository.save(notification);
        return notificationMapper.toDto(savedNotification);
    }
}
