package me.geohod.geohodbackend.service;

import java.util.List;
import java.util.UUID;

import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.eventlog.EventType;

public interface IEventLogService {
    EventLog createLogEntry(UUID eventId, EventType type, String payload);

    void createLogEntryAsync(UUID eventId, EventType type, String payload);

    List<EventLog> findUnprocessed(int limit, String processorName);
}