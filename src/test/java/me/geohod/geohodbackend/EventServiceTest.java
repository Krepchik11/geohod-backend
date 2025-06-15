package me.geohod.geohodbackend;

import me.geohod.geohodbackend.data.dto.CreateEventDto;
import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.mapper.EventModelMapper;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.data.model.repository.UserRepository;
import me.geohod.geohodbackend.service.notification.IEventNotificationService;
import me.geohod.geohodbackend.service.IEventService;
import me.geohod.geohodbackend.service.impl.EventService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

class EventServiceTest {
    @Test
    void shouldNotifyAuthorEventCreated() {
        EventModelMapper modelMapper = mock(EventModelMapper.class);
        EventRepository eventRepository = mock(EventRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        IEventNotificationService notificationService = mock(IEventNotificationService.class);
        IEventService eventService = new EventService(modelMapper, eventRepository, userRepository, notificationService);

        UUID authorId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        when(userRepository.existsById(authorId)).thenReturn(true);
        when(eventRepository.save(any())).thenReturn(new Event("Test Event", "Description", Instant.now(), 10, authorId));
        when(modelMapper.map(any())).thenReturn(new EventDto(eventId, authorId, "Test Event", "Description", Instant.now(), 10, 0, Event.Status.ACTIVE));

        eventService.createEvent(new CreateEventDto(authorId, "Test Event", "Description", Instant.now(), 10));

        verify(notificationService, times(1)).notifyAuthorEventCreated(eventId);
    }
}
