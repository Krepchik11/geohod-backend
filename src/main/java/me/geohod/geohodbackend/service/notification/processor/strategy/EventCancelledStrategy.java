package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import me.geohod.geohodbackend.service.notification.processor.strategy.message.MessageFormatter;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.TemplateType;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventCancelledStrategy implements NotificationStrategy {

    private final EventParticipantRepository eventParticipantRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final MessageFormatter messageFormatter;

    @Override
    public Map<String, Object> createParams(Event event, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            boolean notifyParticipants = root.path("notifyParticipants").asBoolean(false);
            
            Map<String, Object> params = new HashMap<>();
            
            if (notifyParticipants) {
                String participantList = buildParticipantList(event.getId());
                params.put("participantList", participantList);
            }
            
            params.put("notifyParticipants", notifyParticipants);
            return params;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse payload for event cancelled params: {}", payload, e);
            Map<String, Object> fallbackParams = new HashMap<>();
            fallbackParams.put("notifyParticipants", false);
            return fallbackParams;
        }
    }

    @Override
    public Collection<UUID> getRecipients(Event event, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            boolean notifyParticipants = root.path("notifyParticipants").asBoolean(false);

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
        } catch (JsonProcessingException e) {
            log.error("Failed to parse payload for event cancelled recipients: {}", payload, e);
            return Collections.singleton(event.getAuthorId());
        }
    }

    @Override
    public String formatMessage(Event event, User author, Map<String, Object> params) {
        try {
            boolean hasParticipants = params.containsKey("participantList") && 
                params.get("participantList") != null && 
                !((String) params.get("participantList")).isEmpty();
            
            String templateId = hasParticipants ?
                "event.cancelled.organizer.notify-participants" :
                "event.cancelled.organizer.not-notify-participants";
                
            return messageFormatter.formatMessageFromTemplate(templateId,
                    TemplateType.TELEGRAM, event, author, params);
        } catch (Exception e) {
            log.error("Failed to format event cancelled message: {}", e.getMessage());
            return messageFormatter.formatMessageFromTemplate("event.cancelled",
                    TemplateType.TELEGRAM, event, author, params);
        }
    }

    @Override
    public StrategyNotificationType getType() {
        return StrategyNotificationType.EVENT_CANCELLED;
    }

    @Override
    public boolean isValid(Event event, String payload) {
        if (event == null) {
            return false;
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            // Valid if payload can be parsed
            return root != null;
        } catch (JsonProcessingException e) {
            log.error("Invalid payload for EVENT_CANCELLED: {}", payload, e);
            return false;
        }
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
                .collect(java.util.stream.Collectors.joining(", "));
    }

}
