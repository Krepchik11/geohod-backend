package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.IEventParticipationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventParticipationService implements IEventParticipationService {
    private final EventParticipantRepository eventParticipantRepository;
    private final EventRepository eventRepository;
    private final IEventLogService eventLogService;

    @Override
    @Transactional
    public void registerForEvent(UUID userId, UUID eventId) {
        boolean userAlreadyParticipated = eventParticipantRepository.existsByEventIdAndUserId(eventId, userId);

        if (userAlreadyParticipated) {
            throw new IllegalArgumentException("User already participated");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));

        if (event.isCanceled()) {
            throw new IllegalStateException("Registration closed, event cancelled");
        }

        if (event.isFinished()) {
            throw new IllegalStateException("Registration closed, event finished");
        }

        event.increaseParticipantCount();

        eventRepository.save(event);

        EventParticipant participant = new EventParticipant(eventId, userId);
        eventParticipantRepository.save(participant);

        String payload = String.format("{\"userId\": \"%s\", \"eventId\": \"%s\"}", userId, eventId);
        eventLogService.createLogEntry(eventId, EventType.EVENT_REGISTERED, payload);
    }

    @Override
    @Transactional
    public void unregisterFromEvent(UUID userId, UUID eventId) {
        EventParticipant participant = eventParticipantRepository
                .findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found for this event"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));

        if (event.isCanceled() || event.isFinished()) {
            throw new IllegalStateException("Event already closed");
        }

        eventParticipantRepository.delete(participant);

        event.decreaseParticipantCount();
        eventRepository.save(event);

        String payload = String.format("{\"userId\": \"%s\", \"eventId\": \"%s\"}", userId, eventId);
        eventLogService.createLogEntry(eventId, EventType.EVENT_UNREGISTERED, payload);
    }

    @Override
    @Transactional
    public void unregisterParticipantFromEvent(UUID participantId, UUID eventId) {
        EventParticipant participant = eventParticipantRepository.findByEventIdAndId(eventId, participantId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found for this event"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));

        if (event.isCanceled() || event.isFinished()) {
            throw new IllegalStateException("Event already closed");
        }

        eventParticipantRepository.delete(participant);

        event.decreaseParticipantCount();
        eventRepository.save(event);

        String payload = String.format("{\"userId\": \"%s\", \"eventId\": \"%s\"}", participant.getUserId(), eventId);
        eventLogService.createLogEntry(eventId, EventType.EVENT_UNREGISTERED, payload);
    }
}
