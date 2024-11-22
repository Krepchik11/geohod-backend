package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.data.dto.EventDetailedProjection;

import java.util.List;
import java.util.UUID;

public interface IEventProjectionService {
    EventDetailedProjection event(UUID eventId);

    List<EventDetailedProjection> events(UUID participantUserId);
}
