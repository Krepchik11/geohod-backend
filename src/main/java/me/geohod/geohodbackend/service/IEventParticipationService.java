package me.geohod.geohodbackend.service;

import java.util.UUID;

public interface IEventParticipationService {
    void registerForEvent(UUID userId, UUID eventId);

    void unregisterFromEvent(UUID userId, UUID eventId);
}
