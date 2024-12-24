package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.dto.EventDetailedProjection;
import me.geohod.geohodbackend.data.model.repository.EventProjectionRepository;
import me.geohod.geohodbackend.service.IEventProjectionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
    public Page<EventDetailedProjection> events(EventsDetailedProjectionFilter filter, Pageable pageable) {
        return eventProjectionRepository.events(
                filter.authorUserId(),
                filter.statuses(),
                pageable
        );
    }
}
