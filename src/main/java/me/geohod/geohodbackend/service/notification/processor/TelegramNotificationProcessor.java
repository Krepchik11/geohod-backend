package me.geohod.geohodbackend.service.notification.processor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.ITelegramOutboxMessagePublisher;
import me.geohod.geohodbackend.service.IUserService;
import me.geohod.geohodbackend.service.notification.INotificationProcessorProgressService;
import me.geohod.geohodbackend.service.notification.processor.strategy.NotificationStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyRegistry;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationProcessor {
    private static final String PROCESSOR_NAME = "TELEGRAM_NOTIFICATION_PROCESSOR";

    private final IEventLogService eventLogService;
    private final ITelegramOutboxMessagePublisher telegramOutboxMessagePublisher;
    private final EventRepository eventRepository;
    private final IUserService userService;
    private final StrategyRegistry strategyRegistry;
    private final INotificationProcessorProgressService progressService;

    @Scheduled(fixedDelayString = "${geohod.processor.telegram.delay:5000}")
    @Transactional
    public void process() {
        log.trace("Starting Telegram notification processing");
        List<EventLog> unprocessedLogs = eventLogService.findUnprocessed(100, PROCESSOR_NAME);

        for (EventLog eventLog : unprocessedLogs) {
            processEventLog(eventLog);
        }

        if (!unprocessedLogs.isEmpty()) {
            EventLog lastProcessedLog = unprocessedLogs.get(unprocessedLogs.size() - 1);
            progressService.updateProgress(PROCESSOR_NAME, lastProcessedLog.getCreatedAt(), lastProcessedLog.getId());
        }
        log.trace("Finished Telegram notification processing");
    }

    private void processEventLog(EventLog eventLog) {
        eventRepository.findById(eventLog.getEventId()).ifPresent(event -> {
            StrategyNotificationType strategyType = StrategyNotificationType.fromEventType(eventLog.getType());
            if (strategyType == null) {
                log.debug("No strategy type found for event type: {}", eventLog.getType());
                return;
            }

            strategyRegistry.getStrategy(strategyType).ifPresentOrElse(
                strategy -> processWithStrategy(strategy, event, eventLog),
                () -> log.warn("No strategy found for type: {}", strategyType)
            );
        });
    }

    private void processWithStrategy(NotificationStrategy strategy, Event event, EventLog eventLog) {
        String payload = eventLog.getPayload().value();
        
        if (!strategy.isValid(event, payload)) {
            log.warn("Strategy {} cannot handle event {} with payload: {}", 
                strategy.getType(), event.getId(), payload);
            return;
        }

        try {
            Map<String, Object> params = strategy.createParams(event, payload);
            var author = userService.getUser(event.getAuthorId());
            String message = strategy.formatMessage(event, author, params);
            Collection<UUID> recipients = strategy.getRecipients(event, payload);
            
            recipients.forEach(userId -> {
                try {
                    telegramOutboxMessagePublisher.publish(userId, message);
                    log.debug("Published notification for user {} via strategy {}", userId, strategy.getType());
                } catch (Exception e) {
                    log.error("Failed to publish notification for user {} via strategy {}: {}", 
                        userId, strategy.getType(), e.getMessage(), e);
                }
            });
            
        } catch (Exception e) {
            log.error("Error processing event log {} with strategy {}: {}", 
                eventLog.getId(), strategy.getType(), e.getMessage(), e);
        }
    }
}
