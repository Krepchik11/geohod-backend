package me.geohod.geohodbackend;

import me.geohod.geohodbackend.data.mapper.EventParticipantModelMapper;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.IEventParticipationService;
import me.geohod.geohodbackend.service.impl.EventParticipationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EventParticipantServiceTest {
    @Test
    void shouldThrowExceptionWhenEventIsFull() {
        EventRepository eventRepository = Mockito.mock(EventRepository.class);
        IEventParticipationService service = new EventParticipationService(
                Mockito.mock(EventParticipantModelMapper.class),
                Mockito.mock(EventParticipantRepository.class),
                eventRepository
        );

        UUID userId = UUID.randomUUID();

        Event event = new Event("Test Event", "Description", Instant.now(), 1, userId);
        event.increaseParticipantCount();

        UUID eventId = event.getId();
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(IllegalStateException.class, () -> {
            service.registerForEvent(userId, eventId);
        });

        verify(eventRepository, never()).save(any());
    }

    @Test
    void shouldUnregisterParticipant() {
        EventRepository eventRepository = Mockito.mock(EventRepository.class);
        EventParticipantModelMapper participantModelMapper = Mockito.mock(EventParticipantModelMapper.class);
        EventParticipantRepository participantRepository = Mockito.mock(EventParticipantRepository.class);
        IEventParticipationService service = new EventParticipationService(participantModelMapper, participantRepository, eventRepository);

        UUID userId = UUID.randomUUID();

        Event event = spy(new Event("Test Event", "Description", Instant.now(), 10, userId));
        UUID eventId = event.getId();
        event.increaseParticipantCount();
        EventParticipant participant = new EventParticipant(eventId, userId);

        when(participantRepository.findByEventIdAndUserId(eventId, userId))
                .thenReturn(Optional.of(participant));
        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        service.unregisterFromEvent(userId, eventId);

        verify(participantRepository, times(1)).delete(participant);
        verify(eventRepository, times(1)).save(event);
        verify(event, times(1)).decreaseParticipantCount();
    }
}
