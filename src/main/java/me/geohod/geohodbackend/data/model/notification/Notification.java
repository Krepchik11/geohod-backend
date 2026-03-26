package me.geohod.geohodbackend.data.model.notification;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.geohod.geohodbackend.data.model.eventlog.JsonbString;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@Table("notifications")
public class Notification implements Persistable<Long> {
    @Version
    private Long version;
    @Id
    private Long id;

    private UUID eventId;
    private UUID userId;
    private StrategyNotificationType type;
    private JsonbString payload;
    private boolean read;
    private Instant createdAt;

    public Notification(UUID userId, StrategyNotificationType type, String payload) {
        this.userId = userId;
        this.type = type;
        this.payload = new JsonbString(payload);
        this.read = false;
        this.createdAt = Instant.now();
    }

    public Notification(UUID userId, StrategyNotificationType type, JsonbString payload) {
        this.userId = userId;
        this.type = type;
        this.payload = payload;
        this.read = false;
        this.createdAt = Instant.now();
    }

    public Notification(UUID eventId, UUID userId, StrategyNotificationType type, JsonbString payload) {
        this.eventId = eventId;
        this.userId = userId;
        this.type = type;
        this.payload = payload;
        this.read = false;
        this.createdAt = Instant.now();
    }

    public void dismiss() {
        this.read = true;
    }

    @Override
    public boolean isNew() {
        return this.id == null;
    }
}
