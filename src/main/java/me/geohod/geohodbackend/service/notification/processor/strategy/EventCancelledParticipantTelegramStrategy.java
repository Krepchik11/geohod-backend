package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.data.model.repository.UserRepository;
import me.geohod.geohodbackend.service.ITelegramOutboxMessagePublisher;
import me.geohod.geohodbackend.service.IUserService;
import me.geohod.geohodbackend.service.notification.NotificationChannel;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.MessageFormatter;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.TemplateType;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventCancelledParticipantTelegramStrategy implements NotificationStrategy {

    private final GeohodProperties properties;
    private final EventParticipantRepository eventParticipantRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final MessageFormatter messageFormatter;
    private final ITelegramOutboxMessagePublisher telegramOutboxMessagePublisher;
    private final IUserService userService;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.TELEGRAM;
    }

    @Override
    public void send(Event event, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            boolean notifyParticipants = root.path("notifyParticipants").asBoolean(false);

            // Only execute if notifyParticipants is true
            if (!notifyParticipants) {
                log.trace("Skipping {} - notifyParticipants is false", getClass().getSimpleName());
                return;
            }

            Map<String, Object> params = new HashMap<>();
            String eventLink = createEventLink(event);
            params.put("eventLink", eventLink);

            var author = userService.getUser(event.getAuthorId());
            String message = messageFormatter.formatMessageFromTemplate(
                    "event.cancelled",
                    TemplateType.TELEGRAM, event, author, params);

            // Send only to participants, not the organizer
            Set<UUID> participantIds = getParticipants(event);
            participantIds.forEach(userId -> publishMessage(userId, message));

        } catch (JsonProcessingException e) {
            log.error("Failed to parse payload for EVENT_CANCELLED (participant): {}", payload, e);
        }
    }

    private String createEventLink(Event event) throws JsonProcessingException {
        var eventLinkAction = objectMapper.createObjectNode();
        eventLinkAction.put("action", "open");
        eventLinkAction.put("eventId", event.getId().toString());

        String eventLinkString = objectMapper.writeValueAsString(eventLinkAction);
        String eventLinkBase64 = Base64.getEncoder().encodeToString(eventLinkString.getBytes());

        String eventLink = properties.linkTemplates().startappLink() + eventLinkBase64;
        return eventLink;
    }

    private Set<UUID> getParticipants(Event event) {
        return eventParticipantRepository.findEventParticipantByEventId(event.getId()).stream()
                .map(EventParticipant::getUserId)
                .collect(Collectors.toSet());
    }

    private void publishMessage(UUID userId, String message) {
        try {
            telegramOutboxMessagePublisher.publish(userId, message);
            log.debug("Published notification for user {} via strategy {}", userId, getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Failed to publish notification for user {} via strategy {}: {}",
                    userId, getClass().getSimpleName(), e.getMessage(), e);
        }
    }
}