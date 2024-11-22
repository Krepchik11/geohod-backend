package me.geohod.geohodbackend.service;

import jakarta.annotation.Nullable;
import me.geohod.geohodbackend.data.dto.EventDetailedProjection;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IEventProjectionService {
    EventDetailedProjection event(UUID eventId);

    List<EventDetailedProjection> events(EventsDetailedProjectionFilter filter, Pageable pageable);

    record EventsDetailedProjectionFilter(
            @Nullable UUID participantUserId
    ) {
    }
}
