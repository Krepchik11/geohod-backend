package me.geohod.geohodbackend.data.model.review;

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
@Table("reviews")
public class Review {
    @Id
    private UUID id;

    @Version
    private Long version;

    private UUID eventId;
    private UUID authorId;
    private UUID targetUserId;
    private int rating;
    private String comment;
    private boolean isHidden;
    private Instant createdAt;

    public Review(UUID eventId, UUID authorId, UUID targetUserId, int rating, String comment) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.authorId = authorId;
        this.targetUserId = targetUserId;
        this.rating = rating;
        this.comment = comment;
        this.isHidden = false;
        this.createdAt = Instant.now();
    }

    public void hide() {
        this.isHidden = true;
    }

    public void unhide() {
        this.isHidden = false;
    }
} 