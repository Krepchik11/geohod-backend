package me.geohod.geohodbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.notification.Notification;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.IAppNotificationService;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.INotificationProcessorProgressService;
import me.geohod.geohodbackend.service.processor.InAppNotificationProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class InAppNotificationProcessorTest {
    @Mock
    private IEventLogService eventLogService;
    @Mock
    private IAppNotificationService appNotificationService;
    @Mock
    private INotificationProcessorProgressService progressService;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventParticipantRepository eventParticipantRepository;
    
    private InAppNotificationProcessor processor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        processor = new InAppNotificationProcessor(
            eventLogService, 
            appNotificationService, 
            progressService,
            eventRepository,
            eventParticipantRepository
        );
    }

    @Test
    void testProcessWithEventCreatedLog() {
        UUID eventId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        
        EventLog log = mock(EventLog.class);
        when(log.getEventId()).thenReturn(eventId);
        when(log.getType()).thenReturn(me.geohod.geohodbackend.data.model.eventlog.EventType.EVENT_CREATED);
        when(log.getPayload()).thenReturn("payload");
        when(log.getId()).thenReturn(logId);
        
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(eventId);
        when(event.getAuthorId()).thenReturn(authorId);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        
        when(eventLogService.findUnprocessed(anyInt(), anyString())).thenReturn(List.of(log));
        
        processor.process();
        
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(appNotificationService, times(1)).createNotification(notificationCaptor.capture());
        
        Notification capturedNotification = notificationCaptor.getValue();
        assert capturedNotification.getUserId().equals(authorId);
        verify(progressService, times(1)).updateProgress(anyString(), eq(logId));
    }

    @Test
    void testProcessWithParticipantRegisteredLog() {
        UUID eventId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        
        EventLog log = mock(EventLog.class);
        when(log.getEventId()).thenReturn(eventId);
        when(log.getType()).thenReturn(me.geohod.geohodbackend.data.model.eventlog.EventType.EVENT_REGISTERED);
        when(log.getPayload()).thenReturn("{\"userId\": \"" + participantId + "\"}");
        when(log.getId()).thenReturn(logId);
        
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(eventId);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        
        when(eventLogService.findUnprocessed(anyInt(), anyString())).thenReturn(List.of(log));
        
        processor.process();
        
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(appNotificationService, times(1)).createNotification(notificationCaptor.capture());
        
        Notification capturedNotification = notificationCaptor.getValue();
        assert capturedNotification.getUserId().equals(participantId);
        verify(progressService, times(1)).updateProgress(anyString(), eq(logId));
    }

    @Test
    void testProcessWithEventCancelledLog() {
        UUID eventId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        UUID logId = UUID.randomUUID();
        
        EventLog log = mock(EventLog.class);
        when(log.getEventId()).thenReturn(eventId);
        when(log.getType()).thenReturn(me.geohod.geohodbackend.data.model.eventlog.EventType.EVENT_CANCELED);
        when(log.getPayload()).thenReturn("payload");
        when(log.getId()).thenReturn(logId);
        
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(eventId);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        
        EventParticipant participant = mock(EventParticipant.class);
        when(participant.getUserId()).thenReturn(participantId);
        when(eventParticipantRepository.findEventParticipantByEventId(eventId)).thenReturn(List.of(participant));
        
        when(eventLogService.findUnprocessed(anyInt(), anyString())).thenReturn(List.of(log));
        
        processor.process();
        
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(appNotificationService, times(1)).createNotification(notificationCaptor.capture());
        
        Notification capturedNotification = notificationCaptor.getValue();
        assert capturedNotification.getUserId().equals(participantId);
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