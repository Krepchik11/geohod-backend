package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.data.dto.EventParticipantDto;

import java.util.List;
import java.util.UUID;

public interface IParticipantProjectionService {
    List<EventParticipantDto> eventParticipants(UUID eventId);
}
