package me.geohod.geohodbackend.service;

import java.util.List;
import java.util.UUID;

import me.geohod.geohodbackend.data.model.repository.EventParticipantProjectionRepository.EventParticipantProjection;

public interface IParticipantProjectionService {
    List<EventParticipantProjection> eventParticipants(UUID eventId);
}
