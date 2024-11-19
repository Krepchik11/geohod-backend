package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.data.Event;

import java.time.Instant;
import java.util.UUID;

public interface IEventService {
    Event createEvent(UUID authorId, String name, String description, Instant date, int maxParticipants);

    void updateEventDetails(UUID id, String name, String description, Instant date, int maxParticipants);

    void cancelEvent(UUID eventId);
}
