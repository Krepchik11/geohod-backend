package me.geohod.geohodbackend;

import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.data.model.repository.EventLogRepository;
import me.geohod.geohodbackend.data.model.repository.NotificationProcessorProgressRepository;
import me.geohod.geohodbackend.service.impl.EventLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class EventLogServiceTest {
    @Mock
    private EventLogRepository eventLogRepository;
    @Mock
    private NotificationProcessorProgressRepository progressRepository;
    private EventLogServiceImpl eventLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventLogService = new EventLogServiceImpl(eventLogRepository, progressRepository);
    }

    @Test
    void testCreateLogEntry() {
        UUID eventId = UUID.randomUUID();
        EventType type = EventType.EVENT_REGISTERED;
        String payload = "{\"userId\":\"abc\"}";
        eventLogService.createLogEntry(eventId, type, payload);
        ArgumentCaptor<EventLog> captor = ArgumentCaptor.forClass(EventLog.class);
        verify(eventLogRepository, times(1)).save(captor.capture());
        EventLog saved = captor.getValue();
        assertEquals(eventId, saved.getEventId());
        assertEquals(type, saved.getType());
        assertEquals(payload, saved.getPayload());
    }

    @Test
    void testFindUnprocessed() {
        when(progressRepository.findByProcessorName(anyString())).thenReturn(java.util.Optional.empty());
        // Should return empty list as per current implementation
        java.util.List<EventLog> result = eventLogService.findUnprocessed(10, "processor");
        assertTrue(result.isEmpty());
        verify(progressRepository, times(1)).findByProcessorName("processor");
    }
} 