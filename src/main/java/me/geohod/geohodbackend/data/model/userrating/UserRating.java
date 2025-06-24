package me.geohod.geohodbackend.data.model.userrating;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@Table("user_ratings")
public class UserRating {
    @Id
    private UUID id;

    @Version
    private Long version;

    private UUID userId;
    private BigDecimal averageRating;
    private int totalReviewsCount;
    private Instant createdAt;
    private Instant updatedAt;

    public UserRating(UUID userId, BigDecimal averageRating, int totalReviewsCount) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.averageRating = averageRating;
        this.totalReviewsCount = totalReviewsCount;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateRating(BigDecimal averageRating, int totalReviewsCount) {
        this.averageRating = averageRating;
        this.totalReviewsCount = totalReviewsCount;
        this.updatedAt = Instant.now();
    }
} 