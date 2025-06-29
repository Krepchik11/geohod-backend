package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.api.dto.review.ReviewCreateRequest;
import me.geohod.geohodbackend.data.dto.ReviewWithAuthorDto;
import me.geohod.geohodbackend.data.model.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IReviewService {
    Review submitReview(UUID authorId, ReviewCreateRequest request);
    void hideReview(UUID reviewId, UUID userId);
    void unhideReview(UUID reviewId, UUID userId);
    Page<Review> getReviewsForUser(UUID userId, Pageable pageable);
    Page<ReviewWithAuthorDto> getReviewsWithAuthorForUser(UUID userId, Pageable pageable);
} 