package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.data.dto.EventParticipantProjection;

import java.util.List;
import java.util.UUID;

public interface IParticipantProjectionService {
    List<EventParticipantProjection> eventParticipants(UUID eventId);
}
