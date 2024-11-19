package me.geohod.geohodbackend.service.impl;

import me.geohod.geohodbackend.data.Event;
import me.geohod.geohodbackend.data.repository.EventRepository;
import me.geohod.geohodbackend.data.repository.UserRepository;
import me.geohod.geohodbackend.service.IEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class EventService implements IEventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public Event createEvent(UUID authorId, String name, String description, Instant date, int maxParticipants) {
        if (!userRepository.existsById(authorId)) {
            throw new IllegalArgumentException("User does not exist");
        }

        Event event = new Event(name, description, date, maxParticipants, authorId);
        eventRepository.save(event);

        return event;
    }

    @Override
    public void updateEventDetails(UUID id, String name, String description, Instant date, int maxParticipants) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));

        event.updateDetails(name, description, date, maxParticipants);
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
