package me.geohod.geohodbackend;

import me.geohod.geohodbackend.data.dto.UserRatingDto;
import me.geohod.geohodbackend.data.model.userrating.UserRating;
import me.geohod.geohodbackend.data.model.repository.UserRatingRepository;
import me.geohod.geohodbackend.data.model.repository.ReviewRepository;
import me.geohod.geohodbackend.service.impl.UserRatingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserRatingServiceTest {
    @Mock
    private UserRatingRepository userRatingRepository;
    @Mock
    private ReviewRepository reviewRepository;
    private UserRatingServiceImpl userRatingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userRatingService = new UserRatingServiceImpl(userRatingRepository, reviewRepository);
    }

    @Test
    void testGetUserRating() {
        UUID userId = UUID.randomUUID();
        UserRating rating = new UserRating(userId, new BigDecimal("4.5"), 10);
        when(userRatingRepository.findByUserId(userId)).thenReturn(Optional.of(rating));
        
        UserRatingDto result = userRatingService.getUserRating(userId);
        
        assertEquals(userId, result.userId());
        assertEquals(new BigDecimal("4.5"), result.averageRating());
        assertEquals(10, result.totalReviewsCount());
    }

    @Test
    void testGetUserRatingNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRatingRepository.findByUserId(userId)).thenReturn(Optional.empty());
        
        UserRatingDto result = userRatingService.getUserRating(userId);
        
        assertEquals(userId, result.userId());
        assertEquals(BigDecimal.ZERO, result.averageRating());
        assertEquals(0, result.totalReviewsCount());
    }

    @Test
    void testUpdateUserRating() {
        UUID userId = UUID.randomUUID();
        BigDecimal averageRating = new BigDecimal("4.2");
        Long totalCount = 5L;
        
        ReviewRepository.ReviewRatingProjection projection = mock(ReviewRepository.ReviewRatingProjection.class);
        when(projection.getAverageRating()).thenReturn(averageRating);
        when(projection.getTotalCount()).thenReturn(totalCount);
        when(reviewRepository.calculateUserRating(userId)).thenReturn(projection);
        
        UserRating existingRating = new UserRating(userId, BigDecimal.ZERO, 0);
        when(userRatingRepository.findByUserId(userId)).thenReturn(Optional.of(existingRating));
        when(userRatingRepository.save(any(UserRating.class))).thenReturn(existingRating);
        
        userRatingService.updateUserRating(userId);
        
        verify(userRatingRepository).save(any(UserRating.class));
    }

    @Test
    void testUpdateUserRatingNewUser() {
        UUID userId = UUID.randomUUID();
        BigDecimal averageRating = new BigDecimal("3.8");
        Long totalCount = 2L;
        
        ReviewRepository.ReviewRatingProjection projection = mock(ReviewRepository.ReviewRatingProjection.class);
        when(projection.getAverageRating()).thenReturn(averageRating);
        when(projection.getTotalCount()).thenReturn(totalCount);
        when(reviewRepository.calculateUserRating(userId)).thenReturn(projection);
        
        when(userRatingRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRatingRepository.save(any(UserRating.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        userRatingService.updateUserRating(userId);
        
        verify(userRatingRepository).save(any(UserRating.class));
    }
} 