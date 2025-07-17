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

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@Table("notification_processor_progress")
public class NotificationProcessorProgress implements Persistable<UUID> {
    @Version
    private Long version;
    @Id
    private UUID id;

    private String processorName;
    private Instant lastProcessedCreatedAt;
    private UUID lastProcessedId;
    private Instant updatedAt;

    public NotificationProcessorProgress(String processorName, Instant lastProcessedCreatedAt, UUID lastProcessedId) {
        this.id = UUID.randomUUID();
        this.processorName = processorName;
        this.lastProcessedCreatedAt = lastProcessedCreatedAt;
        this.lastProcessedId = lastProcessedId;
        this.updatedAt = Instant.now();
    }

    public void updateProgress(Instant lastProcessedCreatedAt, UUID lastProcessedId) {
        this.lastProcessedCreatedAt = lastProcessedCreatedAt;
        this.lastProcessedId = lastProcessedId;
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean isNew() {
        return version == null;
    }
}
