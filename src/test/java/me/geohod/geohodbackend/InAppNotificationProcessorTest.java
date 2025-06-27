package me.geohod.geohodbackend;

import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.notification.Notification;
import me.geohod.geohodbackend.service.IAppNotificationService;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.INotificationProcessorProgressService;
import me.geohod.geohodbackend.service.processor.InAppNotificationProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InAppNotificationProcessorTest {
    @Mock
    private IEventLogService eventLogService;
    @Mock
    private IAppNotificationService appNotificationService;
    @Mock
    private INotificationProcessorProgressService progressService;
    private InAppNotificationProcessor processor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        processor = new InAppNotificationProcessor(eventLogService, appNotificationService, progressService);
    }

    @Test
    void testProcessWithUnprocessedLogs() {
        EventLog log = mock(EventLog.class);
        UUID eventId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        when(log.getEventId()).thenReturn(eventId);
        when(log.getType()).thenReturn(me.geohod.geohodbackend.data.model.eventlog.EventType.EVENT_REGISTERED);
        when(log.getPayload()).thenReturn("payload");
        when(log.getId()).thenReturn(logId);
        when(eventLogService.findUnprocessed(anyInt(), anyString())).thenReturn(List.of(log));
        processor.process();
        verify(appNotificationService, times(1)).createNotification(any(Notification.class));
        verify(progressService, times(1)).updateProgress(anyString(), eq(logId));
    }

    @Test
    void testProcessWithNoUnprocessedLogs() {
        when(eventLogService.findUnprocessed(anyInt(), anyString())).thenReturn(List.of());
        processor.process();
        verify(appNotificationService, never()).createNotification(any(Notification.class));
        verify(progressService, never()).updateProgress(anyString(), any(UUID.class));
    }
} 