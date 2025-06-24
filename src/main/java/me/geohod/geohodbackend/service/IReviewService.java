package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.api.dto.review.ReviewCreateRequest;
import me.geohod.geohodbackend.data.model.review.Review;

import java.util.UUID;

public interface IReviewService {
    Review submitReview(UUID authorId, ReviewCreateRequest request);
    void hideReview(UUID reviewId);
    void unhideReview(UUID reviewId);
} 