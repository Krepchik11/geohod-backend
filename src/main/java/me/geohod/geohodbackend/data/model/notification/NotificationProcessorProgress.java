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
@Table("notification_processor_progress")
public class NotificationProcessorProgress {
    @Id
    private UUID id;

    @Version
    private Long version;

    private String processorName;
    private UUID lastProcessedEventLogId;
    private Instant updatedAt;

    public NotificationProcessorProgress(String processorName, UUID lastProcessedEventLogId) {
        this.id = UUID.randomUUID();
        this.processorName = processorName;
        this.lastProcessedEventLogId = lastProcessedEventLogId;
        this.updatedAt = Instant.now();
    }

    public void updateProgress(UUID lastProcessedEventLogId) {
        this.lastProcessedEventLogId = lastProcessedEventLogId;
        this.updatedAt = Instant.now();
    }
} 