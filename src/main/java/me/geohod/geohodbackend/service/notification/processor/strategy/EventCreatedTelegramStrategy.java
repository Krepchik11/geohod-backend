package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
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
public class EventCreatedTelegramStrategy implements NotificationStrategy {

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
            Map<String, Object> params = new HashMap<>();

            String registerLink = createRegisterLink(event);
            params.put("registerLink", registerLink);

            String eventLink = createEventLink(event);
            params.put("eventLink", eventLink);

            var author = userService.getUser(event.getAuthorId());
            String message = messageFormatter.formatMessageFromTemplate("event.created",
                    TemplateType.TELEGRAM, event, author, params);

            publishMessage(event.getAuthorId(), message);
        } catch (Exception e) {
            log.error("Failed to create event notification for event {}: {}", event.getId(), e.getMessage(), e);
        }
    }

    private String createRegisterLink(Event event) throws JsonProcessingException {
        var registerAction = objectMapper.createObjectNode();
        registerAction.put("action", "register");
        registerAction.put("eventId", event.getId().toString());

        String registerActionString = objectMapper.writeValueAsString(registerAction);
        String registerActionBase64 = Base64.getEncoder().encodeToString(registerActionString.getBytes());

        return properties.linkTemplates().startappLink() + registerActionBase64;
    }

    private String createEventLink(Event event) throws JsonProcessingException {
        var eventLinkAction = objectMapper.createObjectNode();
        eventLinkAction.put("action", "open");
        eventLinkAction.put("eventId", event.getId().toString());

        String eventLinkString = objectMapper.writeValueAsString(eventLinkAction);
        String eventLinkBase64 = Base64.getEncoder().encodeToString(eventLinkString.getBytes());

        return properties.linkTemplates().startappLink() + eventLinkBase64;
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
