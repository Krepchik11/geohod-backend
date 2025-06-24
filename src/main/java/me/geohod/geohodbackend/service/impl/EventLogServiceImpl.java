package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.data.model.notification.NotificationProcessorProgress;
import me.geohod.geohodbackend.data.model.repository.EventLogRepository;
import me.geohod.geohodbackend.data.model.repository.NotificationProcessorProgressRepository;
import me.geohod.geohodbackend.service.IEventLogService;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
        UUID lastProcessedId = progressRepository.findByProcessorName(processorName)
                .map(NotificationProcessorProgress::getLastProcessedEventLogId)
                .orElse(null);

        if (lastProcessedId == null) {
            // How to handle this? For now, let's assume we need a custom query in the repo.
            // This will be implemented in a future step.
        }
        // Also need a custom query here to find logs GREATER THAN lastProcessedId.
        return Collections.emptyList();
    }
} 