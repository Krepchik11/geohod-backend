package me.geohod.geohodbackend.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.ITelegramOutboxMessagePublisher;
import me.geohod.geohodbackend.service.IUserService;
import me.geohod.geohodbackend.service.notification.EventContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventNotificationService implements IEventNotificationService {
    private static final String EVENT_NOT_FOUND_MESSAGE = "Event {} not found, skipping notification";
    private static final String NOTIFICATION_FAILED_MESSAGE = "Failed to publish message to user {}: {}";

    private final GeohodProperties properties;
    private final EventParticipantRepository eventParticipantRepository;
    private final ITelegramOutboxMessagePublisher outboxMessagePublisher;
    private final EventRepository eventRepository;
    private final IUserService userService;

    @Override
    @Deprecated(since = "2.0", forRemoval = true)
    public void notifyParticipantsEventCancelled(UUID eventId) {
        notifyEvent(eventId, NotificationType.EVENT_CANCELLED, NotificationParams.empty(), this::getEventParticipants);
    }

    @Override
    @Deprecated(since = "2.0", forRemoval = true)
    public void notifyParticipantRegisteredOnEvent(UUID userId, UUID eventId) {
        notifyEvent(eventId, NotificationType.PARTICIPANT_REGISTERED, NotificationParams.empty(), event -> List.of(userId));
    }

    @Override
    @Deprecated(since = "2.0", forRemoval = true)
    public void notifyParticipantUnregisteredFromEvent(UUID userId, UUID eventId) {
        notifyEvent(eventId, NotificationType.PARTICIPANT_UNREGISTERED, NotificationParams.empty(), event -> List.of(userId));
    }

    @Override
    @Deprecated(since = "2.0", forRemoval = true)
    public void notifyAuthorEventCreated(UUID eventId) {
        String linkTemplate = properties.linkTemplates().eventRegistrationLink();
        String botName = properties.telegramBot().username();
        notifyEvent(eventId,
                NotificationType.EVENT_CREATED,
                NotificationParams.eventCreatedParams(eventId, botName, linkTemplate),
                event -> Collections.singleton(event.getAuthorId()));
    }

    @Override
    @Deprecated(since = "2.0", forRemoval = true)
    public void notifyParticipantsEventFinishedWithDonation(UUID eventId, String donationInfo) {
        notifyEvent(eventId,
                NotificationType.EVENT_FINISHED,
                NotificationParams.eventFinishedParams(donationInfo),
                this::getEventParticipants);
    }

    private void notifyEvent(UUID eventId, NotificationType type, NotificationParams params, Function<Event, Collection<UUID>> recipientProvider) {
        eventRepository.findById(eventId)
                .ifPresentOrElse(
                        event -> sendNotifications(event, type, params, recipientProvider),
                        () -> log.warn(EVENT_NOT_FOUND_MESSAGE, eventId)
                );
    }

    private void sendNotifications(Event event, NotificationType type, NotificationParams params, Function<Event, Collection<UUID>> recipientProvider) {
        EventContext context = createEventContext(event);
        String message = type.formatMessage(context, params);

        recipientProvider.apply(event)
                .forEach(userId -> sendNotification(userId, message));
    }

    private void sendNotification(UUID userId, String message) {
        try {
            outboxMessagePublisher.publish(userId, message);
        } catch (Exception e) {
            log.error(NOTIFICATION_FAILED_MESSAGE, userId, e.getMessage(), e);
        }
    }

    private EventContext createEventContext(Event event) {
        User author = userService.getUser(event.getAuthorId());
        return new EventContext(event, author);
    }

    private Collection<UUID> getEventParticipants(Event event) {
        return eventParticipantRepository.findEventParticipantByEventId(event.getId()).stream()
                .map(EventParticipant::getUserId)
                .toList();
    }
}