package me.geohod.geohodbackend.service.notification.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.data.dto.NotificationCreateDto;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.notification.IAppNotificationService;
import me.geohod.geohodbackend.service.notification.INotificationProcessorProgressService;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;

@Component
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationProcessor {
    private static final String PROCESSOR_NAME = "IN_APP_NOTIFICATION_PROCESSOR";

    private final IEventLogService eventLogService;
    private final IAppNotificationService appNotificationService;
    private final INotificationProcessorProgressService progressService;
    private final EventRepository eventRepository;
    private final EventParticipantRepository eventParticipantRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelayString = "${geohod.processor.in-app.delay:5000}")
    @Transactional
    public void process() {
        log.trace("Starting in-app notification processing");
        List<EventLog> unprocessedLogs = eventLogService.findUnprocessed(100, PROCESSOR_NAME);

        for (EventLog eventLog : unprocessedLogs) {
            try {
                processEventLog(eventLog);
            } catch (Exception e) {
                log.error("Error processing event log {}: {}", eventLog.getId(), e.getMessage(), e);
            }
        }

        if (!unprocessedLogs.isEmpty()) {
            EventLog lastProcessedLog = unprocessedLogs.get(unprocessedLogs.size() - 1);
            progressService.updateProgress(PROCESSOR_NAME, lastProcessedLog.getCreatedAt(), lastProcessedLog.getId());
        }
        log.trace("Finished in-app notification processing");
    }

    private void processEventLog(EventLog eventLog) {
        eventRepository.findById(eventLog.getEventId()).ifPresent(event -> {
            StrategyNotificationType type = mapEventTypeToNotificationType(eventLog.getType());
            if (type == null) {
                log.trace("Skipping event log {} - no notification type mapping for {}", eventLog.getId(), eventLog.getType());
                return;
            }

            Collection<UUID> recipients = getRecipients(event, type, eventLog.getPayload().value());
            
            for (UUID userId : recipients) {
                NotificationCreateDto request = new NotificationCreateDto(
                    userId,
                    type,
                    eventLog.getPayload().value(),
                    eventLog.getEventId()
                );
                appNotificationService.createNotification(request);
            }
            
            log.trace("Created {} notifications for event log {}", recipients.size(), eventLog.getId());
        });
    }

    private StrategyNotificationType mapEventTypeToNotificationType(EventType eventType) {
        return StrategyNotificationType.fromEventType(eventType);
    }

    private Collection<UUID> getRecipients(Event event, StrategyNotificationType type, String payload) {
        if (type == StrategyNotificationType.EVENT_CREATED) {
            return Collections.singleton(event.getAuthorId());
        }
        if (type == StrategyNotificationType.PARTICIPANT_REGISTERED || type == StrategyNotificationType.PARTICIPANT_UNREGISTERED) {
            try {
                JsonNode root = objectMapper.readTree(payload);
                return Collections.singleton(UUID.fromString(root.path("userId").asText()));
            } catch (JsonProcessingException e) {
                log.error("Failed to parse payload for participant notification: {}", payload, e);
                return Collections.emptyList();
            }
        }
        // For cancelled and finished, notify all participants
        return eventParticipantRepository.findEventParticipantByEventId(event.getId()).stream()
                .map(EventParticipant::getUserId)
                .collect(Collectors.toSet());
    }
}
