package me.geohod.geohodbackend;

import me.geohod.geohodbackend.data.model.notification.Notification;
import me.geohod.geohodbackend.data.model.repository.NotificationRepository;
import me.geohod.geohodbackend.service.impl.AppNotificationServiceImpl;
import me.geohod.geohodbackend.service.notification.NotificationType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;
    private AppNotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notificationService = new AppNotificationServiceImpl(notificationRepository);
    }

    @Test
    void testFetchNotifications() {
        UUID userId = UUID.randomUUID();
        Notification notification = new Notification(userId, NotificationType.EVENT_CREATED, "payload");
        when(notificationRepository.findByUserIdAndIsReadOrderByIdDesc(eq(userId), eq(false), any())).thenReturn(Collections.singletonList(notification));
        var result = notificationService.getNotifications(userId, 10, false, null);
        assertEquals(1, result.size());
        assertEquals(NotificationType.EVENT_CREATED, result.get(0).getType());
        assertEquals("payload", result.get(0).getPayload());
    }

    @Test
    void testDismiss() {
        Long notificationId = 1L;
        UUID userId = UUID.randomUUID();
        Notification notification = new Notification(userId, NotificationType.EVENT_CREATED, "payload");
        when(notificationRepository.findByIdAndUserId(notificationId, userId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        notificationService.dismiss(notificationId, userId);
        assertTrue(notification.isRead());
    }
} 