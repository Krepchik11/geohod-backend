package me.geohod.geohodbackend.service.notification.processor;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.notification.INotificationProcessorProgressService;
import me.geohod.geohodbackend.service.notification.NotificationChannel;
import me.geohod.geohodbackend.service.notification.processor.strategy.NotificationStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyRegistry;

@Component
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationProcessor {
    private static final String PROCESSOR_NAME = "IN_APP_NOTIFICATION_PROCESSOR";

    private final IEventLogService eventLogService;
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
            List<NotificationStrategy> strategies = strategyRegistry.getStrategies(eventLog.getType(),
                    NotificationChannel.IN_APP);

            if (strategies.isEmpty()) {
                log.trace("No In-App strategies found for event type: {}", eventLog.getType());
                return;
            }

            for (NotificationStrategy strategy : strategies) {
                try {
                    strategy.send(event, eventLog.getPayload().value());
                } catch (Exception e) {
                    log.error("Error processing event log {} with strategy {}: {}",
                            eventLog.getId(), strategy.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
        });
    }
}
