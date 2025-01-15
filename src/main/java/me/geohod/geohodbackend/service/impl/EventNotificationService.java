package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.dto.EventParticipantDto;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.service.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventNotificationService implements IEventNotificationService {
    private final IEventParticipationService participationService;
    private final ITelegramOutboxMessagePublisher outboxMessagePublisher;
    private final IEventService eventService;
    private final IUserService userService;

    @Override
    public void notifyParticipantsEventCancelled(UUID eventId) {
        EventDto event = eventService.event(eventId);
        User author = userService.getUser(event.authorId());
        String message = """
                Организатор отменил мероприятие %s (%s)
                Дополнительную информацию вы можете уточнить у организатора: %s @%s
                """.formatted(event.name(), LocalDate.ofInstant(event.date(), ZoneId.systemDefault()), String.join(" ", author.getFirstName(), author.getLastName()), author.getTgUsername());

        participationService.getParticipantsForEvent(eventId).stream()
                .map(EventParticipantDto::userId)
                .forEach(userId -> outboxMessagePublisher.publish(userId, message));
    }

    @Override
    public void notifyParticipantRegisteredOnEvent(UUID userId, UUID eventId) {
        EventDto event = eventService.event(eventId);
        User author = userService.getUser(event.authorId());
        String message = """
                Вы зарегистрировались на мероприятие %s (%s)
                Организатор: %s @%s
                """.formatted(event.name(), LocalDate.ofInstant(event.date(), ZoneId.systemDefault()), String.join(" ", author.getFirstName(), author.getLastName()), author.getTgUsername());

        outboxMessagePublisher.publish(userId, message);
    }

    @Override
    public void notifyParticipantUnregisteredFromEvent(UUID userId, UUID eventId) {
        EventDto event = eventService.event(eventId);
        String message = """
                Вы отменили регистрацию на мероприятие %s (%s)
                """.formatted(event.name(), LocalDate.ofInstant(event.date(), ZoneId.systemDefault()));

        outboxMessagePublisher.publish(userId, message);
    }

    @Override
    public void notifyParticipantsEventFinished(UUID eventId) {
        EventDto event = eventService.event(eventId);
        String message = """
                Мероприятие %s (%s) завершено
                """.formatted(event.name(), LocalDate.ofInstant(event.date(), ZoneId.systemDefault()));

        participationService.getParticipantsForEvent(eventId).stream()
                .map(EventParticipantDto::userId)
                .forEach(userId -> outboxMessagePublisher.publish(userId, message));
    }

    @Override
    public void notifyAuthorEventCreated(UUID eventId) { // TODO: add link to registration
        EventDto event = eventService.event(eventId);
        User author = userService.getUser(event.authorId());
        String message = """
                Вы создали мероприятие:
                ```
                %s
                %s
                Организатор: %s @%s
                
                Ссылка на регистрацию будет тут!
                ```
                """.formatted(event.name(), LocalDate.ofInstant(event.date(), ZoneId.systemDefault()), String.join(" ", author.getFirstName()), author.getLastName(), author.getTgUsername());

        outboxMessagePublisher.publish(author.getId(), message);
    }
}