package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.dto.EventDetailedProjection;
import me.geohod.geohodbackend.data.model.repository.EventProjectionRepository;
import me.geohod.geohodbackend.service.IEventProjectionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventProjectionService implements IEventProjectionService {
    private final EventProjectionRepository eventProjectionRepository;

    @Override
    public EventDetailedProjection event(UUID eventId) {
        return eventProjectionRepository.event(eventId);
    }

    @Override
    public List<EventDetailedProjection> events(UUID participantUserId) {
        return eventProjectionRepository.events(participantUserId);
    }
}
