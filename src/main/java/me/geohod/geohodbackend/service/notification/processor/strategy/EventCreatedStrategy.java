package me.geohod.geohodbackend.service.notification.processor.strategy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.MessageFormatter;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.TemplateType;

@Component
@RequiredArgsConstructor
public class EventCreatedStrategy implements NotificationStrategy {
    
    private final GeohodProperties properties;
    private final MessageFormatter messageFormatter;
    
    @Override
    public Map<String, Object> createParams(Event event, String payload) {
        Map<String, Object> params = new HashMap<>();
        params.put("eventId", event.getId());
        params.put("botName", properties.telegramBot().username());
        params.put("linkTemplate", properties.linkTemplates().eventRegistrationLink());
        return params;
    }
    
    @Override
    public Collection<UUID> getRecipients(Event event, String payload) {
        // Notify only the event author
        return Collections.singleton(event.getAuthorId());
    }
    
    @Override
    public String formatMessage(Event event, User author, Map<String, Object> params) {
        return messageFormatter.formatMessageFromTemplate("event.created",
            TemplateType.TELEGRAM, event, author, params);
    }
    
    @Override
    public StrategyNotificationType getType() {
        return StrategyNotificationType.EVENT_CREATED;
    }
    
    @Override
    public boolean isValid(Event event, String payload) {
        return event != null;
    }
}
