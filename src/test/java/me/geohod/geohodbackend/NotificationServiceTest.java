package me.geohod.geohodbackend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import me.geohod.geohodbackend.data.dto.NotificationDto;
import me.geohod.geohodbackend.data.mapper.NotificationMapper;
import me.geohod.geohodbackend.data.model.eventlog.JsonbString;
import me.geohod.geohodbackend.data.model.notification.Notification;
import me.geohod.geohodbackend.data.model.repository.NotificationRepository;
import me.geohod.geohodbackend.service.impl.AppNotificationServiceImpl;
import me.geohod.geohodbackend.service.notification.NotificationType;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;

public class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private NotificationMapper notificationMapper;
    
    private AppNotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notificationService = new AppNotificationServiceImpl(notificationRepository, notificationMapper);
    }

    @Test
    void testFetchNotifications() {
        UUID userId = UUID.randomUUID();
        Notification notification = new Notification(userId, StrategyNotificationType.EVENT_CREATED, new JsonbString("payload"));
        // Use NotificationType in DTO for API compatibility
        NotificationDto notificationDto = new NotificationDto(1L, userId, NotificationType.EVENT_CREATED, "payload", false, null, UUID.randomUUID());
        
        when(notificationRepository.findByUserIdAndReadOrderByIdDesc(eq(userId), eq(false), any())).thenReturn(Collections.singletonList(notification));
        when(notificationMapper.toDto(notification)).thenReturn(notificationDto);
        
        var result = notificationService.getNotifications(userId, 10, false, null);
        assertEquals(1, result.size());
        assertEquals(NotificationType.EVENT_CREATED, result.get(0).type());
        assertEquals("payload", result.get(0).payload());
    }

    @Test
    void testDismiss() {
        Long notificationId = 1L;
        UUID userId = UUID.randomUUID();
        Notification notification = new Notification(userId, StrategyNotificationType.EVENT_CREATED, new JsonbString("payload"));
        when(notificationRepository.findByIdAndUserId(notificationId, userId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        notificationService.dismiss(notificationId, userId);
        assertTrue(notification.isRead());
    }
}