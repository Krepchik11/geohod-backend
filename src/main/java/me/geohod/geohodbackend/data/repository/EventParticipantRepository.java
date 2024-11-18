package me.geohod.geohodbackend.data.repository;

import me.geohod.geohodbackend.data.EventParticipant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventParticipantRepository extends CrudRepository<EventParticipant, UUID> {
}
