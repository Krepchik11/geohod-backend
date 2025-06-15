package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.notification.IEventNotificationService;
import me.geohod.geohodbackend.service.IEventParticipationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventParticipationService implements IEventParticipationService {
    private final EventParticipantRepository eventParticipantRepository;
    private final EventRepository eventRepository;
    private final IEventNotificationService notificationService;

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

        notificationService.notifyParticipantRegisteredOnEvent(participant.getUserId(), participant.getEventId());
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

        notificationService.notifyParticipantUnregisteredFromEvent(participant.getUserId(), participant.getEventId());
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

        notificationService.notifyParticipantUnregisteredFromEvent(participant.getUserId(), participant.getEventId());
    }
}
