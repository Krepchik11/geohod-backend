package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.IEventNotificationService;
import me.geohod.geohodbackend.service.ITelegramOutboxMessagePublisher;
import me.geohod.geohodbackend.service.IUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventNotificationService implements IEventNotificationService {

    private static final String EVENT_NOT_FOUND_MESSAGE = "Event {} not found, skipping notification";

    private final EventParticipantRepository eventParticipantRepository;
    private final ITelegramOutboxMessagePublisher outboxMessagePublisher;
    private final EventRepository eventRepository;
    private final IUserService userService;

    @Override
    public void notifyParticipantsEventCancelled(UUID eventId) {
        notifyEvent(eventId, this::formatEventCancelledMessage, this::getEventParticipants);
    }

    @Override
    public void notifyParticipantRegisteredOnEvent(UUID userId, UUID eventId) {
        notifyEvent(eventId, this::formatParticipantRegisteredMessage, _ -> List.of(userId));
    }

    @Override
    public void notifyParticipantUnregisteredFromEvent(UUID userId, UUID eventId) {
        notifyEvent(eventId, this::formatParticipantUnregisteredMessage, _ -> List.of(userId));
    }

    @Override
    public void notifyAuthorEventCreated(UUID eventId) {
        notifyEvent(eventId, this::formatAuthorEventCreatedMessage, this::getEventAuthor);
    }

    private void notifyEvent(UUID eventId, Function<EventContext, String> messageFormatter, Function<Event, Collection<UUID>> recipientProvider) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isEmpty()) {
            log.warn(EVENT_NOT_FOUND_MESSAGE, eventId);
            return;
        }

        Event event = optionalEvent.get();
        User author = userService.getUser(event.getAuthorId());
        EventContext context = new EventContext(event, author);
        String message = messageFormatter.apply(context);

        recipientProvider.apply(event).forEach(userId -> {
            try {
                outboxMessagePublisher.publish(userId, message);
            } catch (Exception e) {
                log.error("Failed to publish message to user {}: {}", userId, e.getMessage(), e);
            }
        });
    }

    private Collection<UUID> getEventParticipants(Event event) {
        return eventParticipantRepository.findEventParticipantByEventId(event.getId()).stream()
                .map(EventParticipant::getUserId)
                .toList();
    }

    private Collection<UUID> getEventAuthor(Event event) {
        return List.of(event.getAuthorId());
    }

    private String formatEventCancelledMessage(EventContext context) {
        return String.format("""
                Организатор отменил мероприятие %s (%s)
                Дополнительную информацию вы можете уточнить у организатора: %s @%s
                """, context.event().getName(), getFormattedEventDate(context.event()), context.authorFullName(), context.authorTgUsername());
    }

    private String formatParticipantRegisteredMessage(EventContext context) {
        return String.format("""
                Вы зарегистрировались на мероприятие %s (%s)
                Организатор: %s @%s
                """, context.event().getName(), getFormattedEventDate(context.event()), context.authorFullName(), context.authorTgUsername());
    }

    private String formatParticipantUnregisteredMessage(EventContext context) {
        return String.format("""
                Вы отменили регистрацию на мероприятие %s (%s)
                """, context.event().getName(), getFormattedEventDate(context.event()));
    }

    private String formatAuthorEventCreatedMessage(EventContext context) {
        return String.format("""
                Вы создали мероприятие:
                ```
                %s
                %s
                Организатор: %s @%s
                
                Ссылка на регистрацию будет тут!
                ```
                """, context.event().getName(), getFormattedEventDate(context.event()), context.authorFullName(), context.authorTgUsername());
    }


    private String getFormattedEventDate(Event event) {
        return LocalDate.ofInstant(event.getDate(), ZoneId.systemDefault()).toString();
    }

    private record EventContext(Event event, User author) {
        String authorFullName() {
            return Stream.of(author.getFirstName(), author.getLastName())
                    .filter(name -> name != null && !name.isBlank())
                    .reduce((s1, s2) -> s1 + " " + s2)
                    .orElse("");
        }

        String authorTgUsername() {
            return author.getTgUsername();
        }
    }
}