package me.geohod.geohodbackend.service.impl;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.data.dto.UserRatingDto;
import me.geohod.geohodbackend.data.model.repository.ReviewRepository;
import me.geohod.geohodbackend.data.model.repository.UserRatingRepository;
import me.geohod.geohodbackend.data.model.userrating.UserRating;
import me.geohod.geohodbackend.service.IUserRatingService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRatingServiceImpl implements IUserRatingService {

    private final UserRatingRepository userRatingRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public UserRatingDto getUserRating(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        return userRatingRepository.findByUserId(userId)
                .map(userRating -> new UserRatingDto(
                        userRating.getUserId(),
                        userRating.getAverageRating(),
                        userRating.getTotalReviewsCount()))
                .orElse(new UserRatingDto(userId, BigDecimal.ZERO, 0));
    }
    
    @Override
    @Transactional
    public void updateUserRating(UUID userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            
            // Calculate rating using database aggregation
            ReviewRepository.ReviewRatingProjection ratingProjection = reviewRepository.calculateUserRating(userId);
            
            BigDecimal averageRating = ratingProjection.averageRating();
            int totalReviewsCount = ratingProjection.totalCount().intValue();
            
            // Save or update user rating
            UserRating userRating = userRatingRepository.findByUserId(userId)
                    .orElse(new UserRating(userId, averageRating, totalReviewsCount));
            
            userRating.updateRating(averageRating, totalReviewsCount);
            userRatingRepository.save(userRating);
            
            log.debug("Successfully updated rating for user {}: average={}, count={}", 
                     userId, averageRating, totalReviewsCount);
        } catch (Exception e) {
            log.error("Failed to update rating for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Async
    public void updateUserRatingAsync(UUID userId) {
        try {
            updateUserRating(userId);
        } catch (Exception e) {
            log.error("Async rating update failed for user {}: {}", userId, e.getMessage(), e);
            // Don't re-throw in async method to prevent transaction rollback
        }
    }
}
