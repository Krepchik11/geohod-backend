package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.review.ReviewCreateRequest;
import me.geohod.geohodbackend.data.model.review.Review;
import me.geohod.geohodbackend.data.model.repository.ReviewRepository;
import me.geohod.geohodbackend.service.IReviewService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements IReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    public Review submitReview(UUID authorId, ReviewCreateRequest request) {
        Review review = new Review(
                request.eventId(),
                authorId,
                request.targetUserId(),
                request.rating(),
                request.comment()
        );
        // User rating update logic will be added here later.
        return reviewRepository.save(review);
    }

    @Override
    public void hideReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found: " + reviewId));
        review.hide();
        reviewRepository.save(review);
    }

    @Override
    public void unhideReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found: " + reviewId));
        review.unhide();
        reviewRepository.save(review);
    }
} 