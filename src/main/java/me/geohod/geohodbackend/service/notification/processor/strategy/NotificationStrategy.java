package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import me.geohod.geohodbackend.data.dto.NotificationCreateDto;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.User;

public interface NotificationStrategy {
    // Shared
    StrategyNotificationType getType();

    boolean isValid(Event event, String payload);

    Collection<UUID> getRecipients(Event event, String payload);

    // Telegram specific
    Map<String, Object> createTelegramParams(Event event, String payload);

    String formatTelegramMessage(Event event, User author, Map<String, Object> params);

    // In-App specific
    NotificationCreateDto createInAppNotification(UUID userId, Event event, String payload);
}