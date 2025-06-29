package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.eventlog.EventType;

import java.util.List;
import java.util.UUID;

public interface IEventLogService {
    EventLog createLogEntry(UUID eventId, EventType type, String payload);

    List<EventLog> findUnprocessed(int limit, String processorName);
} 