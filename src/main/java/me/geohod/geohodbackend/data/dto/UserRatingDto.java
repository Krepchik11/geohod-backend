package me.geohod.geohodbackend.data.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record UserRatingDto(
        UUID userId,
        BigDecimal averageRating,
        int totalReviewsCount
) {
    public UserRatingDto {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (averageRating == null) {
            averageRating = BigDecimal.ZERO;
        }
        if (totalReviewsCount < 0) {
            throw new IllegalArgumentException("Total reviews count cannot be negative");
        }
    }
} 