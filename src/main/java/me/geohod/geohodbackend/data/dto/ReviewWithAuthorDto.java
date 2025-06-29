package me.geohod.geohodbackend.data.dto;

import me.geohod.geohodbackend.data.model.review.Review;
import me.geohod.geohodbackend.data.model.User;

import java.time.Instant;
import java.util.UUID;

public record ReviewWithAuthorDto(
        UUID id,
        UUID eventId,
        UUID authorId,
        String authorUsername,
        String authorImageUrl,
        int rating,
        String comment,
        boolean isHidden,
        Instant createdAt
) {
    public static ReviewWithAuthorDto from(Review review, User author) {
        return new ReviewWithAuthorDto(
                review.getId(),
                review.getEventId(),
                review.getAuthorId(),
                author != null ? author.getTgUsername() : null,
                author != null ? author.getTgImageUrl() : null,
                review.getRating(),
                review.getComment(),
                review.isHidden(),
                review.getCreatedAt()
        );
    }
} 