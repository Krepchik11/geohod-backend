package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.service.notification.NotificationChannel;

@Component
public class StrategyRegistry {

    private final Map<EventType, List<NotificationStrategy>> strategies = new EnumMap<>(EventType.class);

    public void register(EventType type, NotificationStrategy strategy) {
        strategies.computeIfAbsent(type, k -> new ArrayList<>()).add(strategy);
    }

    public List<NotificationStrategy> getStrategies(EventType type, NotificationChannel channel) {
        return strategies.getOrDefault(type, List.of()).stream()
                .filter(s -> s.getChannel() == channel)
                .toList();
    }
}
