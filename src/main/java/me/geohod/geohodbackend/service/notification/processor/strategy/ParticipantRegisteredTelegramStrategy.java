package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.service.ITelegramOutboxMessagePublisher;
import me.geohod.geohodbackend.service.IUserService;
import me.geohod.geohodbackend.service.notification.NotificationChannel;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.MessageFormatter;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.TemplateType;

@Component
@Slf4j
@RequiredArgsConstructor
public class ParticipantRegisteredTelegramStrategy implements NotificationStrategy {

    private final GeohodProperties properties;
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
            String userIdStr = root.path("userId").asText();
            if (userIdStr != null && !userIdStr.isEmpty()) {
                Map<String, Object> params = new HashMap<>();

                String eventLink = createEventLink(event);
                params.put("eventLink", eventLink);

                UUID userId = UUID.fromString(userIdStr);

                var author = userService.getUser(event.getAuthorId());
                String message = messageFormatter.formatMessageFromTemplate("participant.registered",
                        TemplateType.TELEGRAM, event, author, params);

                publishMessage(userId, message);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse payload for PARTICIPANT_REGISTERED: {}", payload, e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID in payload for PARTICIPANT_REGISTERED: {}", payload, e);
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
