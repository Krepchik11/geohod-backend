package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventNotificationService implements IEventNotificationService {
    private final EventParticipantRepository eventParticipantRepository;
    private final ITelegramOutboxMessagePublisher outboxMessagePublisher;
    private final EventRepository eventRepository;
    private final IUserService userService;

    @Override
    public void notifyParticipantsEventCancelled(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow();
        User author = userService.getUser(event.getAuthorId());
        String message = """
                Организатор отменил мероприятие %s (%s)
                Дополнительную информацию вы можете уточнить у организатора: %s @%s
                """.formatted(event.getName(), LocalDate.ofInstant(event.getDate(), ZoneId.systemDefault()), String.join(" ", author.getFirstName(), author.getLastName()), author.getTgUsername());

        eventParticipantRepository.findEventParticipantByEventId(eventId).stream()
                .map(EventParticipant::getUserId)
                .forEach(userId -> outboxMessagePublisher.publish(userId, message));
    }

    @Override
    public void notifyParticipantRegisteredOnEvent(UUID userId, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow();
        User author = userService.getUser(event.getAuthorId());
        String message = """
                Вы зарегистрировались на мероприятие %s (%s)
                Организатор: %s @%s
                """.formatted(event.getName(), LocalDate.ofInstant(event.getDate(), ZoneId.systemDefault()), String.join(" ", author.getFirstName(), author.getLastName()), author.getTgUsername());

        outboxMessagePublisher.publish(userId, message);
    }

    @Override
    public void notifyParticipantUnregisteredFromEvent(UUID userId, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow();
        String message = """
                Вы отменили регистрацию на мероприятие %s (%s)
                """.formatted(event.getName(), LocalDate.ofInstant(event.getDate(), ZoneId.systemDefault()));

        outboxMessagePublisher.publish(userId, message);
    }

    @Override
    public void notifyParticipantsEventFinished(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow();
        String message = """
                Мероприятие %s (%s) завершено
                """.formatted(event.getName(), LocalDate.ofInstant(event.getDate(), ZoneId.systemDefault()));

        eventParticipantRepository.findEventParticipantByEventId(eventId).stream()
                .map(EventParticipant::getUserId)
                .forEach(userId -> outboxMessagePublisher.publish(userId, message));
    }

    @Override
    public void notifyAuthorEventCreated(UUID eventId) { // TODO: add link to registration
        Event event = eventRepository.findById(eventId)
                .orElseThrow();
        User author = userService.getUser(event.getAuthorId());
        String message = """
                Вы создали мероприятие:
                ```
                %s
                %s
                Организатор: %s @%s
                
                Ссылка на регистрацию будет тут!
                ```
                """.formatted(event.getName(), LocalDate.ofInstant(event.getDate(), ZoneId.systemDefault()), String.join(" ", author.getFirstName(), author.getLastName()), author.getTgUsername());

        outboxMessagePublisher.publish(author.getId(), message);
    }
}