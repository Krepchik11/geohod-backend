package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
import me.geohod.geohodbackend.data.model.repository.EventParticipantProjectionRepository;
import me.geohod.geohodbackend.data.model.repository.EventParticipantProjectionRepository.EventParticipantContactInfo;
import me.geohod.geohodbackend.service.ITelegramOutboxMessagePublisher;
import me.geohod.geohodbackend.service.IUserService;
import me.geohod.geohodbackend.service.notification.NotificationChannel;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.MessageFormatter;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.TemplateType;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventCancelledOrganizerNotifyParticipantsTelegramStrategy implements NotificationStrategy {

    private final GeohodProperties properties;
    private final EventParticipantProjectionRepository eventParticipantProjectionRepository;
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

            if (!notifyParticipants) {
                log.trace("Skipping {} - notifyParticipants is false", getClass().getSimpleName());
                return;
            }

            Map<String, Object> params = new HashMap<>();
            String participantList = buildParticipantList(event.getId());
            params.put("participantList", participantList);
            params.put("notifyParticipants", true);

            String eventLink = createEventLink(event);
            params.put("eventLink", eventLink);

            var author = userService.getUser(event.getAuthorId());
            String message = messageFormatter.formatMessageFromTemplate(
                    "event.cancelled.organizer.notify-participants",
                    TemplateType.TELEGRAM, event, author, params);

            var sendTo = event.getAuthorId();

            publishMessage(sendTo, message);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse payload for EVENT_CANCELLED (notify participants): {}", payload, e);
        }
    }

    private String createEventLink(Event event) throws JsonProcessingException {
        var eventLinkAction = objectMapper.createObjectNode();
        eventLinkAction.put("action", "open");
        eventLinkAction.put("eventId", event.getId().toString());

        String eventLinkString = objectMapper.writeValueAsString(eventLinkAction);
        String eventLinkBase64 = Base64.getEncoder().encodeToString(eventLinkString.getBytes());

        return properties.linkTemplates().startappLink() + eventLinkBase64;
    }

    private String buildParticipantList(UUID eventId) {
        if (eventId == null) {
            log.warn("No eventId provided to buildParticipantList");
            return "";
        }

        return eventParticipantProjectionRepository.findEventParticipantContactInfoByEventId(eventId)
                .stream()
                .map(this::formatContactInfo)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining("\n"));
    }

    private String formatContactInfo(EventParticipantContactInfo contact) {
        if (contact == null) {
            return null;
        }

        // username if available and not blank
        if (contact.username() != null && !contact.username().isBlank()) {
            return "@" + contact.username();
        }

        // phone number if available and not blank
        if (contact.phoneNumber() != null && !contact.phoneNumber().isBlank()) {
            return contact.phoneNumber();
        }

        return null;
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
