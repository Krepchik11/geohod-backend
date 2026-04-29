package me.geohod.geohodbackend.service.notification;

import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;
import org.springframework.stereotype.Component;

import java.util.Set;

import static me.geohod.geohodbackend.service.notification.NotificationChannel.*;
import static me.geohod.geohodbackend.service.notification.NotificationRole.*;
import static me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType.*;

@Component
public class NotificationSettingsDefaults {

    private static final Set<String> INVALID_COMBINATIONS = Set.of(
            tripletKey(EVENT_CREATED, PARTICIPANT, TELEGRAM),
            tripletKey(EVENT_CREATED, PARTICIPANT, IN_APP),
            tripletKey(EVENT_CREATED, PARTICIPANT, PUSH),
            tripletKey(REGISTRATION_ENDED, PARTICIPANT, TELEGRAM),
            tripletKey(REGISTRATION_ENDED, PARTICIPANT, IN_APP),
            tripletKey(REGISTRATION_ENDED, PARTICIPANT, PUSH),
            tripletKey(PARTICIPANT_REGISTERED, AUTHOR, TELEGRAM),
            tripletKey(PARTICIPANT_UNREGISTERED, AUTHOR, TELEGRAM)
    );

    private static final Set<String> PUSH_DISABLED = Set.of(
            pairKey(EVENT_CREATED, AUTHOR),
            pairKey(PARTICIPANT_REGISTERED, PARTICIPANT),
            pairKey(PARTICIPANT_REGISTERED, AUTHOR),
            pairKey(PARTICIPANT_UNREGISTERED, PARTICIPANT),
            pairKey(PARTICIPANT_UNREGISTERED, AUTHOR)
    );

    public boolean isValid(StrategyNotificationType type, NotificationRole role, NotificationChannel channel) {
        return !INVALID_COMBINATIONS.contains(tripletKey(type, role, channel));
    }

    public boolean getDefaultEnabled(StrategyNotificationType type, NotificationRole role, NotificationChannel channel) {
        if (channel == PUSH) {
            return !PUSH_DISABLED.contains(pairKey(type, role));
        }
        return true;
    }

    private static String tripletKey(StrategyNotificationType type, NotificationRole role, NotificationChannel channel) {
        return type.name() + ":" + role.name() + ":" + channel.name();
    }

    private static String pairKey(StrategyNotificationType type, NotificationRole role) {
        return type.name() + ":" + role.name();
    }
}
