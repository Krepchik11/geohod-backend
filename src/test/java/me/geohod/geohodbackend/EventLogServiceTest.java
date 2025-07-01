package me.geohod.geohodbackend;

import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.data.model.repository.EventLogRepository;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.data.model.repository.NotificationProcessorProgressRepository;
import me.geohod.geohodbackend.data.model.repository.UserRepository;
import me.geohod.geohodbackend.service.impl.EventLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class EventLogServiceTest extends AbstractIntegrationTest {
    @Mock
    private EventLogRepository eventLogRepository;
    @Mock
    private NotificationProcessorProgressRepository progressRepository;
    private EventLogServiceImpl eventLogService;

    @Autowired(required = false)
    private EventLogRepository realEventLogRepository;

    @Autowired(required = false)
    private EventRepository eventRepository;

    @Autowired(required = false)
    private UserRepository userRepository;

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
        assertEquals(payload, saved.getPayload().value());
    }

    @Test
    void testFindUnprocessed() {
        when(progressRepository.findByProcessorName(anyString())).thenReturn(java.util.Optional.empty());
        // Should return empty list as per current implementation
        java.util.List<EventLog> result = eventLogService.findUnprocessed(10, "processor");
        assertTrue(result.isEmpty());
        verify(progressRepository, times(1)).findByProcessorName("processor");
    }

    @Test
    @Transactional
    void integrationTestPersistAndRetrieveJsonbPayload() {
        if (realEventLogRepository == null) return;
        EventType type = EventType.EVENT_CREATED;
        String payload = "{\"foo\": \"bar\"}";
        User user = new User("id", "username", "firstname", "lastname", "image");
        userRepository.save(user);
        Event event = new Event("name", "description", Instant.now(), 1, user.getId());
        eventRepository.save(event);
        EventLog log = new EventLog(event.getId(), type, payload);
        realEventLogRepository.save(log);
        EventLog found = realEventLogRepository.findById(log.getId()).orElseThrow();
        assertEquals(payload, found.getPayload().value());
        assertEquals(event.getId(), found.getEventId());
        assertEquals(type, found.getType());
    }
} 