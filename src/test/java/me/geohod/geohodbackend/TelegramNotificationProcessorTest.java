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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.eventlog.EventLog;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.notification.INotificationProcessorProgressService;
import me.geohod.geohodbackend.service.notification.NotificationChannel;
import me.geohod.geohodbackend.service.notification.processor.TelegramNotificationProcessor;
import me.geohod.geohodbackend.service.notification.processor.strategy.NotificationStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyRegistry;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationProcessorTest {

        @Test
        void testProcessCallsStrategyAndPublishesNotification(
                        @Mock IEventLogService eventLogService,
                        @Mock EventRepository eventRepository,
                        @Mock StrategyRegistry strategyRegistry,
                        @Mock INotificationProcessorProgressService notificationProcessorProgressService) {

                TelegramNotificationProcessor processor = new TelegramNotificationProcessor(
                                eventLogService,
                                eventRepository,
                                strategyRegistry,
                                notificationProcessorProgressService);

                UUID eventId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();
                UUID authorId = UUID.randomUUID();
                EventLog log = new EventLog(eventId, EventType.EVENT_REGISTERED,
                                String.format("{\"userId\": \"%s\"}", userId));
                Event event = new Event("Test Event", "Description", Instant.now(), 10, authorId);

                when(eventLogService.findUnprocessed(anyInt(), anyString())).thenReturn(List.of(log));
                when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

                NotificationStrategy mockStrategy = mock(NotificationStrategy.class);

                when(strategyRegistry.getStrategies(EventType.EVENT_REGISTERED, NotificationChannel.TELEGRAM))
                                .thenReturn(List.of(mockStrategy));

                processor.process();

                verify(strategyRegistry).getStrategies(EventType.EVENT_REGISTERED, NotificationChannel.TELEGRAM);
                verify(mockStrategy).send(eq(event), eq(log.getPayload().value()));
        }

        @Test
        void testProcessDoesNothingWhenNoEventLogs(
                        @Mock IEventLogService eventLogService,
                        @Mock EventRepository eventRepository,
                        @Mock StrategyRegistry strategyRegistry,
                        @Mock INotificationProcessorProgressService notificationProcessorProgressService) {

                TelegramNotificationProcessor processor = new TelegramNotificationProcessor(
                                eventLogService,
                                eventRepository,
                                strategyRegistry,
                                notificationProcessorProgressService);

                when(eventLogService.findUnprocessed(anyInt(), anyString())).thenReturn(List.of());

                processor.process();

                verify(strategyRegistry, times(0)).getStrategies(any(EventType.class), any(NotificationChannel.class));
        }
}
