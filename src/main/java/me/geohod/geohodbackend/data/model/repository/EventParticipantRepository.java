package me.geohod.geohodbackend.data.model.repository;

import me.geohod.geohodbackend.data.model.EventParticipant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventParticipantRepository extends CrudRepository<EventParticipant, UUID> {
    Optional<EventParticipant> findByEventIdAndId(UUID eventId, UUID id);

    Optional<EventParticipant> findByEventIdAndUserId(UUID eventId, UUID userId);

    List<EventParticipant> findEventParticipantByEventId(UUID eventId);

    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);
}
