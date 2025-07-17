package me.geohod.geohodbackend.data.model.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import me.geohod.geohodbackend.data.model.eventlog.EventLog;

@Repository
public interface EventLogRepository extends CrudRepository<EventLog, UUID> {

    @Query("SELECT * FROM event_logs WHERE (created_at, id) > (:lastCreatedAt, :lastId) ORDER BY created_at ASC, id ASC LIMIT :limit")
    List<EventLog> findUnprocessedAfter(@Param("lastCreatedAt") Instant lastCreatedAt, @Param("lastId") UUID lastId, @Param("limit") int limit);

    @Query("SELECT * FROM event_logs ORDER BY created_at ASC, id ASC LIMIT :limit")
    List<EventLog> findFirstUnprocessed(@Param("limit") int limit);
}
