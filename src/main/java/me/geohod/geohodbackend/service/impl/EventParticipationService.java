package me.geohod.geohodbackend.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.IEventParticipationService;

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
        performUnregister(userId, eventId);
    }

    @Override
    @Transactional
    public void unregisterParticipantFromEvent(UUID participantId, UUID eventId) {
        UUID userId = eventParticipantRepository.findByEventIdAndId(eventId, participantId)
                .map(EventParticipant::getUserId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found for this event"));

        performUnregister(userId, eventId);
    }

    private void performUnregister(UUID userId, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));

        if (event.isCanceled() || event.isFinished()) {
            throw new IllegalStateException("Event already closed");
        }

        int deleted = eventParticipantRepository.deleteByEventIdAndUserId(eventId, userId);
        if (deleted > 0) {
            eventRepository.decrementParticipantCount(eventId);
        } else {
            throw new IllegalArgumentException("Participant not found for this event");
        }

        String payload = String.format("{\"userId\": \"%s\", \"eventId\": \"%s\"}", userId, eventId);
        eventLogService.createLogEntry(eventId, EventType.EVENT_UNREGISTERED, payload);
    }
}
