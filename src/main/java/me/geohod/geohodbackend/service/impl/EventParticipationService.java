package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.IEventParticipationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventParticipationService implements IEventParticipationService {
    private final EventParticipantRepository eventParticipantRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public void registerForEvent(UUID userId, UUID eventId) {
        boolean userAlreadyParticipated = eventParticipantRepository.existsByEventIdAndUserId(eventId, userId);

        if (userAlreadyParticipated) {
            throw new IllegalArgumentException("User already participated");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));

        if (event.getStatus() == Event.Status.CANCELED) {
            throw new IllegalStateException("Can not register for cancelled event");
        }

        event.increaseParticipantCount();

        eventRepository.save(event);

        EventParticipant participant = new EventParticipant(eventId, userId);
        eventParticipantRepository.save(participant);
    }

    @Override
    @Transactional
    public void unregisterFromEvent(UUID userId, UUID eventId) {
        EventParticipant participant = eventParticipantRepository
                .findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found for this event"));

        eventParticipantRepository.delete(participant);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));
        event.decreaseParticipantCount();
        eventRepository.save(event);
    }
}
