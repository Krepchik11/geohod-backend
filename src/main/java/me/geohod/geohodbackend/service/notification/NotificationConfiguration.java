package me.geohod.geohodbackend.service.notification;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.service.notification.processor.strategy.EventCancelledInAppStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.EventCancelledTelegramStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.EventCreatedInAppStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.EventCreatedTelegramStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.EventFinishedInAppStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.EventFinishedTelegramStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.ParticipantRegisteredInAppStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.ParticipantRegisteredTelegramStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.ParticipantUnregisteredInAppStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.ParticipantUnregisteredTelegramStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyRegistry;

@Configuration
@RequiredArgsConstructor
public class NotificationConfiguration {

    private final StrategyRegistry registry;

    private final EventFinishedTelegramStrategy eventFinishedTelegramStrategy;
    private final EventFinishedInAppStrategy eventFinishedInAppStrategy;
    private final EventCancelledTelegramStrategy eventCancelledTelegramStrategy;
    private final EventCancelledInAppStrategy eventCancelledInAppStrategy;
    private final EventCreatedTelegramStrategy eventCreatedTelegramStrategy;
    private final EventCreatedInAppStrategy eventCreatedInAppStrategy;
    private final ParticipantRegisteredTelegramStrategy participantRegisteredTelegramStrategy;
    private final ParticipantRegisteredInAppStrategy participantRegisteredInAppStrategy;
    private final ParticipantUnregisteredTelegramStrategy participantUnregisteredTelegramStrategy;
    private final ParticipantUnregisteredInAppStrategy participantUnregisteredInAppStrategy;

    @PostConstruct
    public void init() {
        registry.register(EventType.EVENT_FINISHED_FOR_REVIEW_LINK, eventFinishedTelegramStrategy);
        registry.register(EventType.EVENT_FINISHED_FOR_REVIEW_LINK, eventFinishedInAppStrategy);

        registry.register(EventType.EVENT_CANCELED, eventCancelledTelegramStrategy);
        registry.register(EventType.EVENT_CANCELED, eventCancelledInAppStrategy);

        registry.register(EventType.EVENT_CREATED, eventCreatedTelegramStrategy);
        registry.register(EventType.EVENT_CREATED, eventCreatedInAppStrategy);

        registry.register(EventType.EVENT_REGISTERED, participantRegisteredTelegramStrategy);
        registry.register(EventType.EVENT_REGISTERED, participantRegisteredInAppStrategy);

        registry.register(EventType.EVENT_UNREGISTERED, participantUnregisteredTelegramStrategy);
        registry.register(EventType.EVENT_UNREGISTERED, participantUnregisteredInAppStrategy);
    }
}
