package me.geohod.geohodbackend;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.IEventParticipationService;
import me.geohod.geohodbackend.service.impl.EventParticipationService;

@ExtendWith(MockitoExtension.class)
public class EventParticipantServiceTest {

    @Mock
    private EventParticipantRepository participantRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private IEventLogService eventLogService;

    @Test
    void testRegisterForEvent() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Event event = new Event("Test Event", "Description", Instant.now(), 10, UUID.randomUUID());

        when(participantRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        IEventParticipationService service = new EventParticipationService(participantRepository, eventRepository, eventLogService);

        // When
        service.registerForEvent(userId, eventId);

        // Then
        verify(eventLogService, times(1)).createLogEntry(eq(eventId), eq(EventType.EVENT_REGISTERED), anyString());
    }

    @Test
    void testUnregisterFromEvent() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Event event = spy(new Event("Test Event", "Description", Instant.now(), 10, UUID.randomUUID()));
        EventParticipant participant = new EventParticipant(eventId, userId);
        event.increaseParticipantCount();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(participantRepository.deleteByEventIdAndUserId(eventId, userId)).thenReturn(1);


        IEventParticipationService service = new EventParticipationService(participantRepository, eventRepository, eventLogService);

        // When
        service.unregisterFromEvent(userId, eventId);

        // Then
        verify(eventLogService, times(1)).createLogEntry(eq(eventId), eq(EventType.EVENT_UNREGISTERED), anyString());
    }
}
