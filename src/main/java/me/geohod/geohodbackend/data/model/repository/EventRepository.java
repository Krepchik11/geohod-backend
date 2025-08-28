package me.geohod.geohodbackend.data.model.repository;

import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import me.geohod.geohodbackend.data.model.Event;

@Repository
public interface EventRepository extends CrudRepository<Event, UUID> {

    // Optimized method for finishing events in a single operation
    @Modifying
    @Query("UPDATE events SET status = 'FINISHED', updated_at = CURRENT_TIMESTAMP WHERE id = :eventId AND status != 'FINISHED'")
    int finishEvent(@Param("eventId") UUID eventId);
}
