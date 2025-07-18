package me.geohod.geohodbackend.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import me.geohod.geohodbackend.api.dto.review.ReviewCreateRequest;
import me.geohod.geohodbackend.data.dto.ReviewWithAuthorDto;
import me.geohod.geohodbackend.data.model.review.Review;

public interface IReviewService {
    Review submitReview(UUID authorId, ReviewCreateRequest request);
    void hideReview(UUID reviewId, UUID userId);
    void unhideReview(UUID reviewId, UUID userId);
    Page<Review> getReviewsForUser(UUID userId, Pageable pageable);
    Page<ReviewWithAuthorDto> getReviewsWithAuthorForUser(UUID userId, UUID requestingUserId, Pageable pageable);
    Optional<Review> getUserReviewForEvent(UUID userId, UUID eventId);
}
