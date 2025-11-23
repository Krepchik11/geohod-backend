package me.geohod.geohodbackend.data.model;

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
@Table("event_participants")
public class EventParticipant implements Persistable<UUID> {
    @Version
    private Long version;
    @Id
    private UUID id;
    private UUID eventId;
    private UUID userId;
    private boolean pollLinkSent;
    private boolean cashDonated;
    private boolean transferDonated;
    private Instant createdAt;

    public EventParticipant(UUID eventId, UUID userId) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.userId = userId;
        this.createdAt = Instant.now();
    }

    public void updateState(boolean pollLinkSent, boolean cashDonated, boolean transferDonated) {
        this.pollLinkSent = pollLinkSent;
        this.cashDonated = cashDonated;
        this.transferDonated = transferDonated;
    }

    @Override
    public boolean isNew() {
        return this.version == null;
    }
}
