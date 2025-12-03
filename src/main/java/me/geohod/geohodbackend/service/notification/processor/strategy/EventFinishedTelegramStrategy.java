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
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.service.ITelegramOutboxMessagePublisher;
import me.geohod.geohodbackend.service.IUserService;
import me.geohod.geohodbackend.service.notification.NotificationChannel;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.MessageFormatter;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.TemplateType;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventFinishedTelegramStrategy implements NotificationStrategy {

    private final GeohodProperties properties;
    private final EventParticipantRepository eventParticipantRepository;
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
            boolean sendPollLink = root.path("sendPollLink").asBoolean(false);

            Map<String, Object> params = new HashMap<>();

            String eventLink = createEventLink(event);
            params.put("eventLink", eventLink);

            if (sendPollLink) {
                String reviewLink = createReviewLink(event);
                params.put("reviewLink", reviewLink);
            }

            var author = userService.getUser(event.getAuthorId());
            String message = messageFormatter.formatMessageFromTemplate("event.finished",
                    TemplateType.TELEGRAM, event, author, params);

            eventParticipantRepository.findEventParticipantByEventId(event.getId()).stream()
                    .map(EventParticipant::getUserId)
                    .forEach(userId -> publishMessage(userId, message));

        } catch (JsonProcessingException e) {
            log.error("Failed to parse payload for EVENT_FINISHED: {}", payload, e);
        } catch (Exception e) {
            log.error("Failed to create finished event notification for event {}: {}", event.getId(), e.getMessage(),
                    e);
        }
    }

    private String createReviewLink(Event event) throws JsonProcessingException {
        var reviewAction = objectMapper.createObjectNode();
        reviewAction.put("action", "review");
        reviewAction.put("eventId", event.getId().toString());

        String reviewString = objectMapper.writeValueAsString(reviewAction);
        String reviewBase64 = Base64.getEncoder().encodeToString(reviewString.getBytes());

        String reviewLink = properties.linkTemplates().startappLink() + reviewBase64;
        return reviewLink;
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
