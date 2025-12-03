package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.data.dto.NotificationCreateDto;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.service.notification.IAppNotificationService;
import me.geohod.geohodbackend.service.notification.NotificationChannel;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventCancelledInAppStrategy implements NotificationStrategy {

    private final EventParticipantRepository eventParticipantRepository;
    private final IAppNotificationService appNotificationService;
    private final ObjectMapper objectMapper;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public void send(Event event, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            boolean notifyParticipants = root.path("notifyParticipants").asBoolean(false);

            Set<UUID> recipients = getRecipients(event, notifyParticipants);

            recipients.forEach(userId -> createNotification(userId, event, payload));

        } catch (JsonProcessingException e) {
            log.error("Failed to parse payload for EVENT_CANCELLED: {}", payload, e);
        }
    }

    private Set<UUID> getRecipients(Event event, boolean notifyParticipants) {
        Set<UUID> recipients = new HashSet<>();
        // Always include the organizer
        recipients.add(event.getAuthorId());

        // Include participants if they should be notified
        if (notifyParticipants) {
            eventParticipantRepository.findEventParticipantByEventId(event.getId()).stream()
                    .map(EventParticipant::getUserId)
                    .forEach(recipients::add);
        }
        return recipients;
    }

    private void createNotification(UUID userId, Event event, String payload) {
        try {
            NotificationCreateDto request = new NotificationCreateDto(
                    userId,
                    StrategyNotificationType.EVENT_CANCELLED,
                    payload,
                    event.getId());
            appNotificationService.createNotification(request);
            log.debug("Created in-app notification for user {} via strategy {}", userId, getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Failed to create in-app notification for user {} via strategy {}: {}",
                    userId, getClass().getSimpleName(), e.getMessage(), e);
        }
    }
}
