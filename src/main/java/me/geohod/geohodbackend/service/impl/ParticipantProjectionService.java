package me.geohod.geohodbackend.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.dto.EventParticipantDto;
import me.geohod.geohodbackend.data.model.repository.ParticipantProjectionRepository;
import me.geohod.geohodbackend.service.IParticipantProjectionService;

@Service
@RequiredArgsConstructor
public class ParticipantProjectionService implements IParticipantProjectionService {
    private final ParticipantProjectionRepository repository;

    @Override
    public List<EventParticipantDto> eventParticipants(UUID eventId) {
        return repository.participants(eventId);
    }
}
