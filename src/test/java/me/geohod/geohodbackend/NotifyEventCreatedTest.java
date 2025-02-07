package me.geohod.geohodbackend;

import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.ITelegramOutboxMessagePublisher;
import me.geohod.geohodbackend.service.IUserService;
import me.geohod.geohodbackend.service.notification.EventNotificationService;
import me.geohod.geohodbackend.service.notification.IEventNotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class NotifyEventCreatedTest {
    @Test
    void checkMessageContainsLink() {
        EventRepository eventRepository = Mockito.mock(EventRepository.class);
        GeohodProperties properties = Mockito.mock(GeohodProperties.class);
        UUID eventId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        String botName = "testBot";
        String linkTemplate = "https://t.me/{botName}/app?startapp=registration_{eventId}";

        when(properties.linkTemplates()).thenReturn(new GeohodProperties.LinkTemplates(linkTemplate));
        when(properties.telegramBot()).thenReturn(new GeohodProperties.TelegramBot(null, botName));

        LocalDate date = LocalDate.of(2025, 1, 9);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(new Event("Grand holidays", null, date.atStartOfDay().toInstant(ZoneOffset.UTC), 1, authorId)));

        IUserService userService = Mockito.mock(IUserService.class);
        when(userService.getUser(authorId)).thenReturn(new User(null, "buxbanner", null, "", null));

        EventParticipantRepository participantRepository = Mockito.mock(EventParticipantRepository.class);

        ITelegramOutboxMessagePublisher outboxMessagePublisher = Mockito.mock(ITelegramOutboxMessagePublisher.class);

        IEventNotificationService eventNotificationService = new EventNotificationService(properties, participantRepository, outboxMessagePublisher, eventRepository, userService);

        eventNotificationService.notifyAuthorEventCreated(eventId);
        Mockito.verify(outboxMessagePublisher, times(1)).publish(authorId, """
                Вы создали мероприятие:
                
                Grand holidays
                2025-01-09
                Организатор: @buxbanner
                
                Ссылка на регистрацию [ЗДЕСЬ](https://t.me/%s/app?startapp=registration_%s)""".formatted(botName, eventId)
        );
    }
}
