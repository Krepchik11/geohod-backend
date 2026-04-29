package me.geohod.geohodbackend.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import me.geohod.geohodbackend.service.notification.NotificationChannel;
import me.geohod.geohodbackend.service.notification.NotificationRole;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Table("user_notification_settings")
public class UserNotificationSetting implements Persistable<UUID> {

    @Version
    private Long version;
    @Id
    private UUID id;
    private UUID userId;
    private StrategyNotificationType type;
    private NotificationRole role;
    private boolean telegram;
    private boolean inApp;
    private boolean push;
    private Instant createdAt;
    private Instant updatedAt;

    public UserNotificationSetting(UUID userId, StrategyNotificationType type, NotificationRole role,
                                   boolean telegram, boolean inApp, boolean push) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.type = type;
        this.role = role;
        this.telegram = telegram;
        this.inApp = inApp;
        this.push = push;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateChannel(NotificationChannel channel, boolean enabled) {
        switch (channel) {
            case TELEGRAM -> this.telegram = enabled;
            case IN_APP -> this.inApp = enabled;
            case PUSH -> this.push = enabled;
        }
        this.updatedAt = Instant.now();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return version == null;
    }
}
