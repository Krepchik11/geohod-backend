package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import me.geohod.geohodbackend.data.dto.NotificationCreateDto;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.MessageFormatter;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.TemplateType;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventFinishedStrategy implements NotificationStrategy {

    private final GeohodProperties properties;
    private final EventParticipantRepository eventParticipantRepository;
    private final ObjectMapper objectMapper;
    private final MessageFormatter messageFormatter;

    @Override
    public Map<String, Object> createTelegramParams(Event event, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            boolean sendPollLink = root.path("sendPollLink").asBoolean(false);

            Map<String, Object> params = new HashMap<>();
            params.put("sendPollLink", sendPollLink);

            if (sendPollLink) {
                params.put("eventId", event.getId());
                params.put("botName", properties.telegramBot().username());
                params.put("linkTemplate", properties.linkTemplates().reviewLink());
            }

            return params;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse payload for EVENT_FINISHED: {}", payload, e);
            return new HashMap<>();
        }
    }

    @Override
    public Collection<UUID> getRecipients(Event event, String payload) {
        return eventParticipantRepository.findEventParticipantByEventId(event.getId()).stream()
                .map(EventParticipant::getUserId)
                .collect(Collectors.toSet());
    }

    @Override
    public String formatTelegramMessage(Event event, User author, Map<String, Object> params) {
        return messageFormatter.formatMessageFromTemplate("event.finished",
                TemplateType.TELEGRAM, event, author, params);
    }

    @Override
    public NotificationCreateDto createInAppNotification(UUID userId, Event event,
            String payload) {
        return new NotificationCreateDto(
                userId,
                StrategyNotificationType.EVENT_FINISHED,
                payload,
                event.getId());
    }

    @Override
    public StrategyNotificationType getType() {
        return StrategyNotificationType.EVENT_FINISHED;
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
            log.error("Invalid payload for EVENT_FINISHED: {}", payload, e);
            return false;
        }
    }
}
