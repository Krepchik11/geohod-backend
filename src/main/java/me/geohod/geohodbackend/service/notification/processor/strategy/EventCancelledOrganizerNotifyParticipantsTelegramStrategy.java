package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.HashMap;
import java.util.HashSet;
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
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.User;
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
public class EventCancelledOrganizerNotifyParticipantsTelegramStrategy implements NotificationStrategy {

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
            String participantList = buildParticipantList(event.getId());
            params.put("participantList", participantList);
            params.put("notifyParticipants", true);

            var author = userService.getUser(event.getAuthorId());
            String message = messageFormatter.formatMessageFromTemplate(
                    "event.cancelled.organizer.notify-participants",
                    TemplateType.TELEGRAM, event, author, params);

            Set<UUID> recipients = getRecipients(event);
            recipients.forEach(userId -> publishMessage(userId, message));

        } catch (JsonProcessingException e) {
            log.error("Failed to parse payload for EVENT_CANCELLED (notify participants): {}", payload, e);
        }
    }

    private Set<UUID> getRecipients(Event event) {
        Set<UUID> recipients = new HashSet<>();
        // Include the organizer
        recipients.add(event.getAuthorId());

        // Include all participants
        eventParticipantRepository.findEventParticipantByEventId(event.getId()).stream()
                .map(EventParticipant::getUserId)
                .forEach(recipients::add);

        return recipients;
    }

    private String buildParticipantList(UUID eventId) {
        return eventParticipantRepository.findEventParticipantByEventId(eventId).stream()
                .map(participant -> {
                    User user = userRepository.findById(participant.getUserId()).orElse(null);
                    if (user != null && user.getTgUsername() != null && !user.getTgUsername().trim().isEmpty()) {
                        return "@" + user.getTgUsername().trim();
                    }
                    return null;
                })
                .filter(username -> username != null)
                .collect(Collectors.joining(", "));
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
