package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.service.ITelegramOutboxMessagePublisher;
import me.geohod.geohodbackend.service.IUserService;
import me.geohod.geohodbackend.service.link.BinaryLinkGenerator;
import me.geohod.geohodbackend.service.link.LinkAction;
import me.geohod.geohodbackend.service.notification.NotificationChannel;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.MessageFormatter;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.TemplateType;

@Component
@Slf4j
@RequiredArgsConstructor
public class ParticipantUnregisteredTelegramStrategy implements NotificationStrategy {

    private final ObjectMapper objectMapper;
    private final BinaryLinkGenerator binaryLinkGenerator;
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
                String message = messageFormatter.formatMessageFromTemplate("participant.unregistered",
                        TemplateType.TELEGRAM, event, author, params);

                publishMessage(userId, message);
            }
        } catch (Exception e) {
            log.error("Failed to create participant unregistered notification for event {}: {}", event.getId(), e.getMessage(), e);
        }
    }

    private String createEventLink(Event event) {
        return binaryLinkGenerator.generateLink(LinkAction.OPEN_EVENT, event.getId());
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
