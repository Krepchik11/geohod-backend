package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.User;

public interface NotificationStrategy {
    Map<String, Object> createParams(Event event, String payload);
    Collection<UUID> getRecipients(Event event, String payload);
    String formatMessage(Event event, User author, Map<String, Object> params);
    StrategyNotificationType getType();
    boolean isValid(Event event, String payload);
}