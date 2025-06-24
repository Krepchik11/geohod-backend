package me.geohod.geohodbackend;

import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.ITelegramOutboxMessagePublisher;
import me.geohod.geohodbackend.service.IUserService;
import me.geohod.geohodbackend.service.processor.TelegramNotificationProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.service.INotificationProcessorProgressService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TelegramNotificationProcessorTest {

    private TelegramNotificationProcessor processor;

    @Mock
    private IEventLogService eventLogService;
    @Mock
    private ITelegramOutboxMessagePublisher telegramOutboxMessagePublisher;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private IUserService userService;
    @Mock
    private GeohodProperties geohodProperties;
    @Mock
    private EventParticipantRepository eventParticipantRepository;

    @BeforeEach
    void setUp() {
        processor = new TelegramNotificationProcessor(
                eventLogService,
                telegramOutboxMessagePublisher,
                mock(INotificationProcessorProgressService.class),
                eventRepository,
                eventParticipantRepository,
                userService,
                geohodProperties
        );
    }

    @Test
    void testProcess() {
        // Given
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        EventLog log = new EventLog(eventId, EventType.EVENT_REGISTERED, String.format("{\"userId\": \"%s\"}", userId));
        Event event = new Event("Test Event", "Description", Instant.now(), 10, authorId);
        User author = new User();

        when(eventLogService.findUnprocessed(anyInt(), anyString())).thenReturn(List.of(log));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userService.getUser(authorId)).thenReturn(author);

        // When
        processor.process();

        // Then
        verify(telegramOutboxMessagePublisher, times(1)).publish(eq(userId), anyString());
    }
} 