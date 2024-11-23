package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.dto.CreateEventDto;
import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.dto.UpdateEventDto;
import me.geohod.geohodbackend.data.mapper.EventModelMapper;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.data.model.repository.UserRepository;
import me.geohod.geohodbackend.service.IEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService implements IEventService {
    private final EventModelMapper mapper;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public EventDto event(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));
        return mapper.map(event);
    }

    @Override
    public EventDto createEvent(CreateEventDto createDto) {
        if (!userRepository.existsById(createDto.authorId())) {
            throw new IllegalArgumentException("User does not exist");
        }

        Event event = new Event(
                createDto.name(),
                createDto.description(),
                createDto.date(),
                createDto.maxParticipants(),
                createDto.authorId()
        );
        eventRepository.save(event);

        return mapper.map(event);
    }

    @Override
    public void updateEventDetails(UpdateEventDto updateDto) {
        Event event = eventRepository.findById(updateDto.eventId())
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));

        event.updateDetails(
                updateDto.name(),
                updateDto.description(),
                updateDto.date(),
                updateDto.maxParticipants()
        );
        eventRepository.save(event);
    }

    @Override
    @Transactional
    public void cancelEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));
        event.cancel();

        eventRepository.save(event);
    }
}
