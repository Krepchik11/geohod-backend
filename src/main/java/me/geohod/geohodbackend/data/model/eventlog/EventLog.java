package me.geohod.geohodbackend.data.model.eventlog;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@Table("event_logs")
public class EventLog implements Persistable<UUID> {
    @Version
    private Long version;
    @Id
    private UUID id;
    
    private UUID eventId;
    private EventType type;
    private JsonbString payload;
    private Instant createdAt;

    public EventLog(UUID eventId, EventType type, String payload) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.type = type;
        this.payload = new JsonbString(payload);
        this.createdAt = Instant.now();
    }

    public EventLog(UUID eventId, EventType type, JsonbString payload) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.type = type;
        this.payload = payload;
        this.createdAt = Instant.now();
    }

    @Override
    public boolean isNew() {
        return this.version == null;
    }
} 