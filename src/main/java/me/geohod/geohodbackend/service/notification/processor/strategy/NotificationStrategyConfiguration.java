package me.geohod.geohodbackend.service.notification.processor.strategy;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class NotificationStrategyConfiguration {
    
    private final StrategyRegistry strategyRegistry;
    private final EventCreatedStrategy eventCreatedStrategy;
    private final EventCancelledStrategy eventCancelledStrategy;
    private final EventFinishedStrategy eventFinishedStrategy;
    private final ParticipantRegisteredStrategy participantRegisteredStrategy;
    private final ParticipantUnregisteredStrategy participantUnregisteredStrategy;
    
    @PostConstruct
    public void registerStrategies() {
        strategyRegistry.registerStrategy(StrategyNotificationType.EVENT_CREATED, eventCreatedStrategy);
        strategyRegistry.registerStrategy(StrategyNotificationType.EVENT_CANCELLED, eventCancelledStrategy);
        strategyRegistry.registerStrategy(StrategyNotificationType.EVENT_FINISHED, eventFinishedStrategy);
        strategyRegistry.registerStrategy(StrategyNotificationType.PARTICIPANT_REGISTERED, participantRegisteredStrategy);
        strategyRegistry.registerStrategy(StrategyNotificationType.PARTICIPANT_UNREGISTERED, participantUnregisteredStrategy);
    }
}
