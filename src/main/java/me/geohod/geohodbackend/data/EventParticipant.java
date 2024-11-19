package me.geohod.geohodbackend.data;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@Table("event_participants")
public class EventParticipant {
    @Id
    private UUID id;
    private UUID eventId;
    private UUID userId;
    private LocalDateTime createdAt;

    public EventParticipant(UUID eventId, UUID userId) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }
}
