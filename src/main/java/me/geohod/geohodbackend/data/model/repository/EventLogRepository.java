package me.geohod.geohodbackend.data.model.repository;

import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventLogRepository extends CrudRepository<EventLog, UUID> {
    
    @Query("SELECT * FROM event_logs WHERE id > :lastProcessedId ORDER BY id ASC LIMIT :limit")
    List<EventLog> findUnprocessedAfterId(@Param("lastProcessedId") UUID lastProcessedId, @Param("limit") int limit);
    
    @Query("SELECT * FROM event_logs ORDER BY id ASC LIMIT :limit")
    List<EventLog> findFirstUnprocessed(@Param("limit") int limit);
} 