package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.data.dto.CreateEventDto;
import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.dto.UpdateEventDto;

import java.util.UUID;

public interface IEventService {
    EventDto event(UUID eventId);

    EventDto createEvent(CreateEventDto createDto);

    void updateEventDetails(UpdateEventDto updateDto);

    void cancelEvent(UUID eventId);
}
