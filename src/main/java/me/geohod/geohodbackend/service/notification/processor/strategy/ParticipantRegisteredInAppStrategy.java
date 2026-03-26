package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.data.dto.NotificationCreateDto;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.service.notification.IAppNotificationService;
import me.geohod.geohodbackend.service.notification.NotificationChannel;

@Component
@Slf4j
@RequiredArgsConstructor
public class ParticipantRegisteredInAppStrategy implements NotificationStrategy {

    private final IAppNotificationService appNotificationService;
    private final ObjectMapper objectMapper;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public void send(Event event, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String userIdStr = root.path("userId").asText();
            if (userIdStr != null && !userIdStr.isEmpty()) {
                UUID userId = UUID.fromString(userIdStr);
                createNotification(userId, event, payload);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse payload for PARTICIPANT_REGISTERED: {}", payload, e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID in payload for PARTICIPANT_REGISTERED: {}", payload, e);
        }
    }

    private void createNotification(UUID userId, Event event, String payload) {
        try {
            NotificationCreateDto request = new NotificationCreateDto(
                    userId,
                    StrategyNotificationType.PARTICIPANT_REGISTERED,
                    payload,
                    event.getId());
            appNotificationService.createNotification(request);
            log.debug("Created in-app notification for user {} via strategy {}", userId, getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Failed to create in-app notification for user {} via strategy {}: {}",
                    userId, getClass().getSimpleName(), e.getMessage(), e);
        }
    }
}
