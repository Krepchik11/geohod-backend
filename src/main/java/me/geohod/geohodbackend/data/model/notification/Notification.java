package me.geohod.geohodbackend.data.model.notification;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.geohod.geohodbackend.service.notification.NotificationType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@Table("notifications")
public class Notification {
    @Id
    private UUID id;

    @Version
    private Long version;

    private UUID userId;
    private NotificationType type;
    private String payload;
    private boolean isRead;
    private Instant createdAt;

    public Notification(UUID userId, NotificationType type, String payload) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.type = type;
        this.payload = payload;
        this.isRead = false;
        this.createdAt = Instant.now();
    }

    public void dismiss() {
        this.isRead = true;
    }
} 