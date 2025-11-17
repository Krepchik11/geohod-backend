package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StrategyRegistry {
    
    private final Map<StrategyNotificationType, NotificationStrategy> strategies = new EnumMap<>(StrategyNotificationType.class);
    
    public void registerStrategy(StrategyNotificationType type, NotificationStrategy strategy) {
        strategies.put(type, strategy);
    }
    
    public Optional<NotificationStrategy> getStrategy(StrategyNotificationType type) {
        return Optional.ofNullable(strategies.get(type));
    }
    
    public boolean hasStrategy(StrategyNotificationType type) {
        return strategies.containsKey(type);
    }
}
