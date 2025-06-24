package me.geohod.geohodbackend.data.model.repository;

import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventLogRepository extends CrudRepository<EventLog, UUID> {
} 