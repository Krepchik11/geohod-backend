package me.geohod.geohodbackend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import me.geohod.geohodbackend.data.dto.NotificationCreateDto;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.notification.IAppNotificationService;
import me.geohod.geohodbackend.service.notification.INotificationProcessorProgressService;
import me.geohod.geohodbackend.service.notification.processor.InAppNotificationProcessor;
import me.geohod.geohodbackend.service.notification.processor.strategy.NotificationStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyRegistry;

class InAppNotificationProcessorTest {
    @Mock
    private IEventLogService eventLogService;
    @Mock
    private IAppNotificationService appNotificationService;
    @Mock
    private INotificationProcessorProgressService progressService;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private StrategyRegistry strategyRegistry;

    private InAppNotificationProcessor processor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        processor = new InAppNotificationProcessor(
                eventLogService,
                appNotificationService,
                progressService,
                eventRepository,
                strategyRegistry);
    }

    @Test
    void testProcessWithEventCreatedLog() {
        UUID eventId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        String payload = "payload";

        EventLog log = mock(EventLog.class);
        when(log.getEventId()).thenReturn(eventId);
        when(log.getType()).thenReturn(me.geohod.geohodbackend.data.model.eventlog.EventType.EVENT_CREATED);
        when(log.getPayload()).thenReturn(new me.geohod.geohodbackend.data.model.eventlog.JsonbString(payload));
        when(log.getId()).thenReturn(logId);
        when(log.getCreatedAt()).thenReturn(Instant.now());

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(eventId);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        when(eventLogService.findUnprocessed(anyInt(), anyString())).thenReturn(List.of(log));

        NotificationStrategy strategy = mock(NotificationStrategy.class);
        when(strategy.isValid(event, payload)).thenReturn(true);
        when(strategy.getRecipients(event, payload)).thenReturn(Collections.singleton(authorId));
        when(strategy.createInAppNotification(authorId, event, payload)).thenReturn(
                new NotificationCreateDto(authorId, StrategyNotificationType.EVENT_CREATED, payload, eventId));

        when(strategyRegistry.getStrategy(StrategyNotificationType.EVENT_CREATED)).thenReturn(Optional.of(strategy));

        processor.process();

        ArgumentCaptor<NotificationCreateDto> requestCaptor = ArgumentCaptor.forClass(NotificationCreateDto.class);
        verify(appNotificationService, times(1)).createNotification(requestCaptor.capture());

        NotificationCreateDto capturedRequest = requestCaptor.getValue();
        assert capturedRequest.userId().equals(authorId);
        verify(progressService, times(1)).updateProgress(anyString(), any(Instant.class), eq(logId));
    }

    @Test
    void testProcessWithNoUnprocessedLogs() {
        when(eventLogService.findUnprocessed(anyInt(), anyString())).thenReturn(List.of());
        processor.process();
        verify(appNotificationService, never()).createNotification(any(NotificationCreateDto.class));
        verify(progressService, never()).updateProgress(anyString(), any(Instant.class), any(UUID.class));
    }
}
