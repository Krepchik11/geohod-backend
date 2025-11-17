package me.geohod.geohodbackend.service;

import java.util.UUID;

import me.geohod.geohodbackend.data.dto.CancelEventDto;
import me.geohod.geohodbackend.data.dto.CreateEventDto;
import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.dto.FinishEventDto;
import me.geohod.geohodbackend.data.dto.UpdateEventDto;

public interface IEventService {
    EventDto event(UUID eventId);

    EventDto createEvent(CreateEventDto createDto);

    void updateEventDetails(UpdateEventDto updateDto);

    void cancelEvent(CancelEventDto cancelDto);

    void finishEvent(FinishEventDto finishDto);
}
