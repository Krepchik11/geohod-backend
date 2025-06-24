package me.geohod.geohodbackend.data.model.notification;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    private String type;
    private String payload;
    private boolean isRead;
    private Instant createdAt;

    public Notification(UUID userId, String type, String payload) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.type = type;
        this.payload = payload;
        this.isRead = false;
        this.createdAt = Instant.now();
    }

    public void markAsRead() {
        this.isRead = true;
    }
} 