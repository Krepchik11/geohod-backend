package me.geohod.geohodbackend.data.model.notification;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.geohod.geohodbackend.service.notification.NotificationType;
import me.geohod.geohodbackend.data.model.eventlog.JsonbString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

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
    private NotificationType type;
    private JsonbString payload;
    private boolean isRead;
    private Instant createdAt;

    public Notification(UUID userId, NotificationType type, String payload) {
        this.userId = userId;
        this.type = type;
        this.payload = new JsonbString(payload);
        this.isRead = false;
        this.createdAt = Instant.now();
    }

    public Notification(UUID userId, NotificationType type, JsonbString payload) {
        this.userId = userId;
        this.type = type;
        this.payload = payload;
        this.isRead = false;
        this.createdAt = Instant.now();
    }

    public Notification(UUID eventId, UUID userId, NotificationType type, JsonbString payload) {
        this.eventId = eventId;
        this.userId = userId;
        this.type = type;
        this.payload = payload;
        this.isRead = false;
        this.createdAt = Instant.now();
    }

    public void dismiss() {
        this.isRead = true;
    }

    @Override
    public boolean isNew() {
        return this.id == null;
    }
} 