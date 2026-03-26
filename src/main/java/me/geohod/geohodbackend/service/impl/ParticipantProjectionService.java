package me.geohod.geohodbackend.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.repository.EventParticipantProjectionRepository;
import me.geohod.geohodbackend.data.model.repository.EventParticipantProjectionRepository.EventParticipantProjection;
import me.geohod.geohodbackend.service.IParticipantProjectionService;

@Service
@RequiredArgsConstructor
public class ParticipantProjectionService implements IParticipantProjectionService {
    private final EventParticipantProjectionRepository repository;

    @Override
    public List<EventParticipantProjection> eventParticipants(UUID eventId) {
        return repository.findEventParticipantByEventId(eventId);
    }
}
