package me.geohod.geohodbackend.service.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.INotificationProcessorProgressService;
import me.geohod.geohodbackend.service.ITelegramOutboxMessagePublisher;
import me.geohod.geohodbackend.service.IUserService;
import me.geohod.geohodbackend.service.notification.EventContext;
import me.geohod.geohodbackend.service.notification.NotificationParams;
import me.geohod.geohodbackend.service.notification.NotificationType;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationProcessor {
    private static final String PROCESSOR_NAME = "TELEGRAM_NOTIFICATION_PROCESSOR";

    private final IEventLogService eventLogService;
    private final ITelegramOutboxMessagePublisher telegramOutboxMessagePublisher;
    private final INotificationProcessorProgressService progressService;
    private final EventRepository eventRepository;
    private final EventParticipantRepository eventParticipantRepository;
    private final IUserService userService;
    private final GeohodProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelayString = "${geohod.processor.telegram.delay:5000}")
    @Transactional
    public void process() {
        log.debug("Starting Telegram notification processing");
        List<EventLog> unprocessedLogs = eventLogService.findUnprocessed(100, PROCESSOR_NAME);

        for (EventLog log : unprocessedLogs) {
            eventRepository.findById(log.getEventId()).ifPresent(event -> {
                NotificationType type = mapEventType(log.getType());
                if (type == null) return;

                EventContext context = new EventContext(event, userService.getUser(event.getAuthorId()));
                NotificationParams params = createParams(type, event.getId(), log.getPayload().value());
                String message = type.formatMessage(context, params);
                Collection<UUID> recipients = getRecipients(event, type, log.getPayload().value());

                recipients.forEach(userId -> telegramOutboxMessagePublisher.publish(userId, message));
            });
        }

        if (!unprocessedLogs.isEmpty()) {
            EventLog lastProcessedLog = unprocessedLogs.get(unprocessedLogs.size() - 1);
            progressService.updateProgress(PROCESSOR_NAME, lastProcessedLog.getCreatedAt(), lastProcessedLog.getId());
        }
        log.debug("Finished Telegram notification processing");
    }

    private NotificationType mapEventType(me.geohod.geohodbackend.data.model.eventlog.EventType eventType) {
        return switch (eventType) {
            case EVENT_CREATED -> NotificationType.EVENT_CREATED;
            case EVENT_CANCELED -> NotificationType.EVENT_CANCELLED;
            case EVENT_REGISTERED -> NotificationType.PARTICIPANT_REGISTERED;
            case EVENT_UNREGISTERED -> NotificationType.PARTICIPANT_UNREGISTERED;
            case EVENT_FINISHED_FOR_REVIEW_LINK -> NotificationType.EVENT_FINISHED;
        };
    }

    private NotificationParams createParams(NotificationType type, UUID eventId, String payload) {
        if (type == NotificationType.EVENT_CREATED) {
            String linkTemplate = properties.linkTemplates().eventRegistrationLink();
            String botName = properties.telegramBot().username();
            return NotificationParams.eventCreatedParams(eventId, botName, linkTemplate);
        }
        if (type == NotificationType.EVENT_FINISHED) {
            try {
                JsonNode root = objectMapper.readTree(payload);
                if (root.path("sendDonationRequest").asBoolean(false)) {
                    return NotificationParams.eventFinishedParams(root.path("donationInfo").asText(""));
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to parse payload for EVENT_FINISHED: {}", payload, e);
            }
        }
        return NotificationParams.empty();
    }

    private Collection<UUID> getRecipients(Event event, NotificationType type, String payload) {
        if (type == NotificationType.EVENT_CREATED) {
            return Collections.singleton(event.getAuthorId());
        }
        if (type == NotificationType.PARTICIPANT_REGISTERED || type == NotificationType.PARTICIPANT_UNREGISTERED) {
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
                .map(me.geohod.geohodbackend.data.model.EventParticipant::getUserId).toList();
    }
}
