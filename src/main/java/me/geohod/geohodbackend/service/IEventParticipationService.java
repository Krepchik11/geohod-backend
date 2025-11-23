package me.geohod.geohodbackend.service;

import java.util.UUID;

import me.geohod.geohodbackend.api.dto.request.UpdateParticipantStateRequest;

public interface IEventParticipationService {
    void registerForEvent(UUID userId, UUID eventId, int participantCount);

    void unregisterFromEvent(UUID userId, UUID eventId);

    void unregisterParticipantFromEvent(UUID participantId, UUID eventId);

    void updateParticipantState(UUID userId, UUID eventId, UpdateParticipantStateRequest request);
}
