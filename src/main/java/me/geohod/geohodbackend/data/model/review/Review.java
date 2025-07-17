package me.geohod.geohodbackend.data.model.review;

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
@Table("reviews")
public class Review implements Persistable<UUID> {
    @Version
    private Long version;
    @Id
    private UUID id;

    private UUID eventId;
    private UUID authorId;
    private int rating;
    private String comment;
    private boolean isHidden;
    private Instant createdAt;

    public Review(UUID eventId, UUID authorId, int rating, String comment) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.authorId = authorId;
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

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean isNew() {
        return version == null;
    }
}
