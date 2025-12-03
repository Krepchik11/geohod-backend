package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.data.dto.NotificationCreateDto;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.service.notification.IAppNotificationService;
import me.geohod.geohodbackend.service.notification.NotificationChannel;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventFinishedInAppStrategy implements NotificationStrategy {

    private final EventParticipantRepository eventParticipantRepository;
    private final IAppNotificationService appNotificationService;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public void send(Event event, String payload) {
        eventParticipantRepository.findEventParticipantByEventId(event.getId()).stream()
                .map(EventParticipant::getUserId)
                .forEach(userId -> createNotification(userId, event, payload));
    }

    private void createNotification(UUID userId, Event event, String payload) {
        try {
            NotificationCreateDto request = new NotificationCreateDto(
                    userId,
                    StrategyNotificationType.EVENT_FINISHED,
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
