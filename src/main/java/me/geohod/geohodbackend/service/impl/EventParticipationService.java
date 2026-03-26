package me.geohod.geohodbackend.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.request.UpdateParticipantStateRequest;
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
    public void registerForEvent(UUID userId, UUID eventId, int participantCount) {
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

        for (int i = 0; i < participantCount; i++) {
            EventParticipant participant = new EventParticipant(eventId, userId);
            eventParticipantRepository.save(participant);
            event.increaseParticipantCount();
        }

        eventRepository.save(event);

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

    @Override
    @Transactional
    public void updateParticipantState(UUID userId, UUID eventId,
            UpdateParticipantStateRequest request) {
        int updatedCount = eventParticipantRepository.updateStateByEventIdAndUserId(
                eventId,
                userId,
                request.pollLinkSent(),
                request.cashDonated(),
                request.transferDonated());

        if (updatedCount == 0) {
            throw new IllegalArgumentException("User is not a participant of this event");
        }
    }

    @Override
    public boolean isUserParticipant(UUID userId, UUID eventId) {
        return eventParticipantRepository.existsByEventIdAndUserId(eventId, userId);
    }

    @Override
    public int getUserParticipantCount(UUID userId, UUID eventId) {
        return eventParticipantRepository.countByEventIdAndUserId(eventId, userId);
    }

    private void performUnregister(UUID userId, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));

        if (event.isCanceled() || event.isFinished()) {
            throw new IllegalStateException("Event already closed");
        }

        int deletedCount = eventParticipantRepository.deleteByEventIdAndUserId(eventId, userId);

        if (deletedCount == 0) {
            throw new IllegalArgumentException("Participant not found for this event");
        }

        for (int i = 0; i < deletedCount; i++) {
            eventRepository.decrementParticipantCount(eventId);
        }

        String payload = String.format("{\"userId\": \"%s\", \"eventId\": \"%s\"}", userId, eventId);
        eventLogService.createLogEntry(eventId, EventType.EVENT_UNREGISTERED, payload);
    }
}
