package me.geohod.geohodbackend.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@Table("events")
public class Event {
    @Id
    private UUID id;
    private UUID authorId;
    private String name;
    private String description;
    private Instant date;
    private int maxParticipants;
    private int currentParticipants;
    private Status status;
    private Instant createdAt;
    private Instant updatedAt;

    public Event(String name, String description, Instant date, int maxParticipants, UUID authorId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.date = date;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = 0;
        this.status = Status.ACTIVE;
        this.authorId = authorId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateDetails(String name, String description, Instant date, int maxParticipants) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.maxParticipants = maxParticipants;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = Status.CANCELED;
        this.updatedAt = Instant.now();
    }

    public void increaseParticipantCount() {
        if (isFull()) {
            throw new IllegalStateException("Event is full. Cannot add more participants.");
        }
        this.currentParticipants++;
        this.updatedAt = Instant.now();
    }

    public void decreaseParticipantCount() {
        if (currentParticipants <= 0) {
            throw new IllegalStateException("Event has zero participants, cannot remove more participants");
        }
        this.currentParticipants--;
        this.updatedAt = Instant.now();
    }

    public boolean isFull() {
        return currentParticipants >= maxParticipants;
    }

    public enum Status {
        ACTIVE, CANCELED
    }
}
