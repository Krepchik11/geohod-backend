package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.data.dto.EventParticipantDto;
import me.geohod.geohodbackend.data.model.EventParticipant;

import java.util.List;
import java.util.UUID;

public interface IEventParticipationService {
    void registerForEvent(UUID userId, UUID eventId);

    void unregisterFromEvent(UUID userId, UUID eventId);

    List<EventParticipantDto> getParticipantsForEvent(UUID eventId);
}
