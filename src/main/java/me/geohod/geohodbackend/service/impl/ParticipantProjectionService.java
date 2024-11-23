package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.dto.TelegramUserDetails;
import me.geohod.geohodbackend.data.model.repository.ParticipantProjectionRepository;
import me.geohod.geohodbackend.service.IParticipantProjectionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParticipantProjectionService implements IParticipantProjectionService {
    private final ParticipantProjectionRepository repository;

    @Override
    public List<TelegramUserDetails> participantsTelegramUserProjection(UUID eventId) {
        return repository.participants(eventId);
    }
}
