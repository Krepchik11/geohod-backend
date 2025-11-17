package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.MessageFormatter;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.TemplateType;

@Component
@Slf4j
@RequiredArgsConstructor
public class ParticipantRegisteredStrategy implements NotificationStrategy {
    
    private final ObjectMapper objectMapper;
    private final MessageFormatter messageFormatter;
    
    @Override
    public Map<String, Object> createParams(Event event, String payload) {
        return new HashMap<>();
    }
    
    @Override
    public Collection<UUID> getRecipients(Event event, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String userIdStr = root.path("userId").asText();
            if (userIdStr != null && !userIdStr.isEmpty()) {
                return Collections.singleton(UUID.fromString(userIdStr));
            }
            return Collections.emptyList();
        } catch (JsonProcessingException e) {
            log.error("Failed to parse payload for PARTICIPANT_REGISTERED: {}", payload, e);
            return Collections.emptyList();
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID in payload for PARTICIPANT_REGISTERED: {}", payload, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public String formatMessage(Event event, User author, Map<String, Object> params) {
        return messageFormatter.formatMessageFromTemplate("participant.registered", 
            TemplateType.TELEGRAM, event, author, params);
    }
    
    @Override
    public StrategyNotificationType getType() {
        return StrategyNotificationType.PARTICIPANT_REGISTERED;
    }
    
    @Override
    public boolean isValid(Event event, String payload) {
        if (event == null) {
            return false;
        }
        
        try {
            JsonNode root = objectMapper.readTree(payload);
            String userIdStr = root.path("userId").asText();
            // Valid if payload contains a userId
            return userIdStr != null && !userIdStr.isEmpty();
        } catch (JsonProcessingException e) {
            log.error("Invalid payload for PARTICIPANT_REGISTERED: {}", payload, e);
            return false;
        }
    }
}
