package me.geohod.geohodbackend;

import me.geohod.geohodbackend.data.model.userrating.UserRating;
import me.geohod.geohodbackend.data.model.repository.UserRatingRepository;
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
    private UserRatingServiceImpl userRatingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userRatingService = new UserRatingServiceImpl(userRatingRepository);
    }

    @Test
    void testGetUserRating() {
        UUID userId = UUID.randomUUID();
        UserRating rating = new UserRating(userId, new BigDecimal("4.5"), 10);
        when(userRatingRepository.findByUserId(userId)).thenReturn(Optional.of(rating));
        UserRating result = userRatingService.getUserRating(userId);
        assertEquals(new BigDecimal("4.5"), result.getAverageRating());
        assertEquals(10, result.getTotalReviewsCount());
    }

    @Test
    void testGetUserRatingNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRatingRepository.findByUserId(userId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userRatingService.getUserRating(userId));
    }
} 