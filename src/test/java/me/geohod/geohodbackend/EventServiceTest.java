package me.geohod.geohodbackend;

import me.geohod.geohodbackend.data.dto.CreateEventDto;
import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.mapper.EventModelMapper;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.data.model.repository.UserRepository;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.IEventService;
import me.geohod.geohodbackend.service.impl.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
    @Mock
    private EventModelMapper modelMapper;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private IEventLogService eventLogService;

    @Test
    public void testCreateEvent() {
        // Given
        UUID authorId = UUID.randomUUID();
        when(userRepository.existsById(authorId)).thenReturn(true);
        IEventService eventService = new EventService(modelMapper, eventRepository, userRepository, eventLogService);
        EventDto mockEventDto = new EventDto(UUID.randomUUID(), authorId, "Test Event", "Description", Instant.now(), 10, 0, Event.Status.ACTIVE);
        when(modelMapper.map(any(Event.class))).thenReturn(mockEventDto);

        // When
        CreateEventDto createDto = new CreateEventDto(authorId, "Test Event", "Description", Instant.now(), 10);
        eventService.createEvent(createDto);

        // Then
        verify(eventLogService, times(1)).createLogEntry(any(UUID.class), eq(EventType.EVENT_CREATED), anyString());
    }
}
