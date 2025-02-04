package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.dto.EventParticipantProjection;
import me.geohod.geohodbackend.data.model.repository.EventParticipantProjectionRepository;
import me.geohod.geohodbackend.service.IParticipantProjectionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParticipantProjectionService implements IParticipantProjectionService {
    private final EventParticipantProjectionRepository repository;

    @Override
    public List<EventParticipantProjection> eventParticipants(UUID eventId) {
        return repository.findEventParticipantByEventId(eventId);
    }
}
