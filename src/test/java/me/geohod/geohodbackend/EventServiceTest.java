package me.geohod.geohodbackend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

@ExtendWith(MockitoExtension.class)
class EventServiceTest {
        @Mock
        private EventModelMapper modelMapper;
        @Mock
        private EventRepository eventRepository;
        @Mock
        private UserRepository userRepository;
        @Mock
        private IEventLogService eventLogService;
        @Mock
        private ObjectMapper objectMapper;

        @Test
        void testCreateEvent() throws JsonProcessingException {
                // Given
                UUID authorId = UUID.randomUUID();
                when(userRepository.existsById(authorId)).thenReturn(true);
                when(objectMapper.writeValueAsString(any())).thenReturn("{}");
                IEventService eventService = new EventService(modelMapper, eventRepository, userRepository,
                                eventLogService,
                                objectMapper);
                EventDto mockEventDto = new EventDto(UUID.randomUUID(), authorId, "Test Event", "Description",
                                Instant.now(),
                                10, 0, Event.Status.ACTIVE);
                when(modelMapper.map(any(Event.class))).thenReturn(mockEventDto);

                // When
                CreateEventDto createDto = new CreateEventDto(authorId, "Test Event", "Description", Instant.now(), 10);
                eventService.createEvent(createDto);

                // Then
                verify(eventLogService, times(1)).createLogEntry(any(UUID.class), eq(EventType.EVENT_CREATED),
                                anyString());
        }

        @Test
        void testFinishEvent() throws JsonProcessingException {
                // Given
                UUID eventId = UUID.randomUUID();
                me.geohod.geohodbackend.data.dto.FinishEventDto finishDto = new me.geohod.geohodbackend.data.dto.FinishEventDto(
                                eventId, true, true, false);
                when(eventRepository.finishEvent(eventId, true, true, false)).thenReturn(1);
                when(objectMapper.writeValueAsString(any())).thenReturn("{}");
                IEventService eventService = new EventService(modelMapper, eventRepository, userRepository,
                                eventLogService,
                                objectMapper);

                // When
                eventService.finishEvent(finishDto);

                // Then
                verify(eventRepository, times(1)).finishEvent(eventId, true, true, false);
                verify(eventLogService, times(1)).createLogEntryAsync(eq(eventId),
                                eq(EventType.EVENT_FINISHED_FOR_REVIEW_LINK),
                                anyString());
        }
}
