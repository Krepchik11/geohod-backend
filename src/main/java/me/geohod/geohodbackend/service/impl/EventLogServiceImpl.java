package me.geohod.geohodbackend.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.data.model.repository.EventLogRepository;
import me.geohod.geohodbackend.data.model.repository.NotificationProcessorProgressRepository;
import me.geohod.geohodbackend.service.IEventLogService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventLogServiceImpl implements IEventLogService {

    private final EventLogRepository eventLogRepository;
    private final NotificationProcessorProgressRepository progressRepository;

    @Override
    public EventLog createLogEntry(UUID eventId, EventType type, String payload) {
        EventLog eventLog = new EventLog(eventId, type, payload);
        return eventLogRepository.save(eventLog);
    }

    @Override
    @Async
    public void createLogEntryAsync(UUID eventId, EventType type, String payload) {
        try {
            EventLog eventLog = new EventLog(eventId, type, payload);
            eventLogRepository.save(eventLog);
        } catch (Exception e) {
            log.error("Failed to create async log entry: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<EventLog> findUnprocessed(int limit, String processorName) {
        // Validate parameters
        if (limit <= 0 || limit > 1000) {
            throw new IllegalArgumentException("Limit must be between 1 and 1000");
        }
        if (processorName == null || processorName.trim().isEmpty()) {
            throw new IllegalArgumentException("Processor name cannot be null or empty");
        }

        // Find the last processed event log marker for this processor
        var progress = progressRepository.findByProcessorName(processorName);

        if (progress.isEmpty()) {
            // No previous processing, return first batch of unprocessed logs
            return eventLogRepository.findFirstUnprocessed(limit);
        } else {
            // Return logs after the last processed marker
            var p = progress.get();
            return eventLogRepository.findUnprocessedAfter(p.getLastProcessedCreatedAt(), p.getLastProcessedId(), limit);
        }
    }
}

