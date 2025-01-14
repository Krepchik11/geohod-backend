package me.geohod.geohodbackend;

import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.dto.EventParticipantDto;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.service.*;
import me.geohod.geohodbackend.service.impl.EventNotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

class NotifyEventCancelledTest {
    @Test
    void checkNotificationMessageCorrect() {
        IEventService eventService = Mockito.mock(IEventService.class);
        UUID eventId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2025, 1, 9);
        when(eventService.event(eventId)).thenReturn(new EventDto(null, authorId, "Grand holidays", null, date.atStartOfDay().toInstant(ZoneOffset.UTC), 1, 1, null));

        IUserService userService = Mockito.mock(IUserService.class);
        when(userService.getUser(authorId)).thenReturn(new User(null, "buxbanner", "Matew", "Kozlov", null));

        UUID participantId = UUID.randomUUID();
        IEventParticipationService participationService = Mockito.mock(IEventParticipationService.class);
        when(participationService.getParticipantsForEvent(eventId)).thenReturn(List.of(new EventParticipantDto(eventId, participantId)));

        ITelegramOutboxMessagePublisher outboxMessagePublisher = Mockito.spy(ITelegramOutboxMessagePublisher.class);

        IEventNotificationService eventNotificationService = new EventNotificationService(participationService, outboxMessagePublisher, eventService, userService);

        eventNotificationService.notifyEventCancelled(eventId);
        Mockito.verify(outboxMessagePublisher).publish(participantId, """
                Организатор отменил мероприятие Grand holidays (2025-01-09)
                Дополнительную информацию вы можете уточнить у организатора: Matew Kozlov @buxbanner
                """);
    }
}
