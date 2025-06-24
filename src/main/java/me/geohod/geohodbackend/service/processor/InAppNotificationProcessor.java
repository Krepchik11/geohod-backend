package me.geohod.geohodbackend.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.IAppNotificationService;
import me.geohod.geohodbackend.service.INotificationProcessorProgressService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.notification.Notification;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationProcessor {
    private static final String PROCESSOR_NAME = "IN_APP_NOTIFICATION_PROCESSOR";

    private final IEventLogService eventLogService;
    private final IAppNotificationService appNotificationService;
    private final INotificationProcessorProgressService progressService;

    @Scheduled(fixedDelayString = "${geohod.processor.in-app.delay:5000}")
    public void process() {
        log.debug("Starting in-app notification processing");
        List<EventLog> unprocessedLogs = eventLogService.findUnprocessed(100, PROCESSOR_NAME);

        for (EventLog log : unprocessedLogs) {
            // This is a simplified mapping. A more robust solution would involve a dedicated mapper.
            Notification notification = new Notification(log.getEventId(), log.getType().toString(), log.getPayload());
            appNotificationService.createNotification(notification);
        }

        if (!unprocessedLogs.isEmpty()) {
            EventLog lastProcessedLog = unprocessedLogs.get(unprocessedLogs.size() - 1);
            progressService.updateProgress(PROCESSOR_NAME, lastProcessedLog.getId());
        }
        log.debug("Finished in-app notification processing");
    }
} 