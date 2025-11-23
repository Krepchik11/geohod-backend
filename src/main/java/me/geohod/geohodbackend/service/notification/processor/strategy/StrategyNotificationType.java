package me.geohod.geohodbackend.service.notification.processor.strategy;

import me.geohod.geohodbackend.data.model.eventlog.EventType;

public enum StrategyNotificationType {
    EVENT_CREATED,
    EVENT_CANCELLED,
    EVENT_FINISHED,
    PARTICIPANT_REGISTERED,
    PARTICIPANT_UNREGISTERED;
    
    public static StrategyNotificationType fromEventType(EventType eventType) {
        return switch (eventType) {
            case EVENT_CREATED -> EVENT_CREATED;
            case EVENT_CANCELED -> EVENT_CANCELLED;
            case EVENT_REGISTERED -> PARTICIPANT_REGISTERED;
            case EVENT_UNREGISTERED -> PARTICIPANT_UNREGISTERED;
            case EVENT_FINISHED_FOR_REVIEW_LINK -> EVENT_FINISHED;
            default -> null;
        };
    }
}