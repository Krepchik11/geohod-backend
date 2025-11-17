package me.geohod.geohodbackend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.ITelegramOutboxMessagePublisher;
import me.geohod.geohodbackend.service.IUserService;
import me.geohod.geohodbackend.service.notification.INotificationProcessorProgressService;
import me.geohod.geohodbackend.service.notification.processor.TelegramNotificationProcessor;
import me.geohod.geohodbackend.service.notification.processor.strategy.NotificationStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyRegistry;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationProcessorTest {

    @Test
    void testProcessCallsStrategyAndPublishesNotification(
            @Mock IEventLogService eventLogService,
            @Mock ITelegramOutboxMessagePublisher telegramOutboxMessagePublisher,
            @Mock EventRepository eventRepository,
            @Mock IUserService userService,
            @Mock StrategyRegistry strategyRegistry,
            @Mock INotificationProcessorProgressService notificationProcessorProgressService) {
        
        TelegramNotificationProcessor processor = new TelegramNotificationProcessor(
                eventLogService,
                telegramOutboxMessagePublisher,
                eventRepository,
                userService,
                strategyRegistry,
                notificationProcessorProgressService
        );
        
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        EventLog log = new EventLog(eventId, EventType.EVENT_REGISTERED, String.format("{\"userId\": \"%s\"}", userId));
        Event event = new Event("Test Event", "Description", Instant.now(), 10, authorId);
        User author = new User();

        when(eventLogService.findUnprocessed(anyInt(), anyString())).thenReturn(List.of(log));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userService.getUser(authorId)).thenReturn(author);
        
        NotificationStrategy mockStrategy = mock(NotificationStrategy.class);
        when(mockStrategy.isValid(event, log.getPayload().value())).thenReturn(true);
        when(mockStrategy.createParams(event, log.getPayload().value())).thenReturn(Map.of());
        when(mockStrategy.getRecipients(event, log.getPayload().value())).thenReturn(Collections.singleton(userId));
        when(mockStrategy.formatMessage(any(Event.class), any(User.class), any(Map.class)))
            .thenReturn("Test notification message");
        
        when(strategyRegistry.getStrategy(StrategyNotificationType.PARTICIPANT_REGISTERED))
            .thenReturn(Optional.of(mockStrategy));

        processor.process();

        verify(strategyRegistry).getStrategy(StrategyNotificationType.PARTICIPANT_REGISTERED);
        verify(mockStrategy).createParams(event, log.getPayload().value());
        verify(mockStrategy).getRecipients(event, log.getPayload().value());
        verify(telegramOutboxMessagePublisher).publish(eq(userId), anyString());
    }

    @Test
    void testProcessDoesNothingWhenNoEventLogs(
            @Mock IEventLogService eventLogService,
            @Mock ITelegramOutboxMessagePublisher telegramOutboxMessagePublisher,
            @Mock EventRepository eventRepository,
            @Mock IUserService userService,
            @Mock StrategyRegistry strategyRegistry,
            @Mock INotificationProcessorProgressService notificationProcessorProgressService) {
        
        TelegramNotificationProcessor processor = new TelegramNotificationProcessor(
                eventLogService,
                telegramOutboxMessagePublisher,
                eventRepository,
                userService,
                strategyRegistry,
                notificationProcessorProgressService
        );

        when(eventLogService.findUnprocessed(anyInt(), anyString())).thenReturn(List.of());

        processor.process();

        verify(telegramOutboxMessagePublisher, times(0)).publish(any(UUID.class), anyString());
    }
}
