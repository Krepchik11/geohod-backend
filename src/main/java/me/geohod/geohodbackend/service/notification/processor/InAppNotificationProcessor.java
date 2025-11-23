package me.geohod.geohodbackend.service.notification.processor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.data.dto.NotificationCreateDto;
import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.notification.IAppNotificationService;
import me.geohod.geohodbackend.service.notification.INotificationProcessorProgressService;
import me.geohod.geohodbackend.service.notification.processor.strategy.NotificationStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyRegistry;

@Component
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationProcessor {
    private static final String PROCESSOR_NAME = "IN_APP_NOTIFICATION_PROCESSOR";

    private final IEventLogService eventLogService;
    private final IAppNotificationService appNotificationService;
    private final INotificationProcessorProgressService progressService;
    private final EventRepository eventRepository;
    private final StrategyRegistry strategyRegistry;

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
            StrategyNotificationType type = StrategyNotificationType.fromEventType(eventLog.getType());
            if (type == null) {
                log.trace("Skipping event log {} - no notification type mapping for {}", eventLog.getId(),
                        eventLog.getType());
                return;
            }

            strategyRegistry.getStrategy(type).ifPresentOrElse(
                    strategy -> processWithStrategy(strategy, event, eventLog),
                    () -> log.warn("No strategy found for type: {}", type));
        });
    }

    private void processWithStrategy(NotificationStrategy strategy, me.geohod.geohodbackend.data.model.Event event,
            EventLog eventLog) {
        String payload = eventLog.getPayload().value();

        if (!strategy.isValid(event, payload)) {
            log.warn("Strategy {} cannot handle event {} with payload: {}",
                    strategy.getType(), event.getId(), payload);
            return;
        }

        Collection<UUID> recipients = strategy.getRecipients(event, payload);

        for (UUID userId : recipients) {
            NotificationCreateDto request = strategy.createInAppNotification(userId, event, payload);
            appNotificationService.createNotification(request);
        }

        log.trace("Created {} notifications for event log {}", recipients.size(), eventLog.getId());
    }
}
