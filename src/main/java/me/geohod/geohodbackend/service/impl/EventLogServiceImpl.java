package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.data.model.notification.NotificationProcessorProgress;
import me.geohod.geohodbackend.data.model.repository.EventLogRepository;
import me.geohod.geohodbackend.data.model.repository.NotificationProcessorProgressRepository;
import me.geohod.geohodbackend.service.IEventLogService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventLogServiceImpl implements IEventLogService {

    private final EventLogRepository eventLogRepository;
    private final NotificationProcessorProgressRepository progressRepository;

    @Override
    public EventLog createLogEntry(UUID eventId, EventType type, String payload) {
        EventLog eventLog = new EventLog(eventId, type, payload);
        return eventLogRepository.save(eventLog);
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

        // Find the last processed event log ID for this processor
        UUID lastProcessedId = progressRepository.findByProcessorName(processorName)
                .map(NotificationProcessorProgress::getLastProcessedEventLogId)
                .orElse(null);

        if (lastProcessedId == null) {
            // No previous processing, return first batch of unprocessed logs
            return eventLogRepository.findFirstUnprocessed(limit);
        } else {
            // Return logs after the last processed ID
            return eventLogRepository.findUnprocessedAfterId(lastProcessedId, limit);
        }
    }
} 