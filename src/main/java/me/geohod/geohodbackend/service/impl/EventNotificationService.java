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
    public void notifyEventCancelled(UUID eventId) {
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


}
