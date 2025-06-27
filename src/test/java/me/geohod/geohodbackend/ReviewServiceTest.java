package me.geohod.geohodbackend;

import me.geohod.geohodbackend.api.dto.review.ReviewCreateRequest;
import me.geohod.geohodbackend.data.model.repository.ReviewRepository;
import me.geohod.geohodbackend.data.model.review.Review;
import me.geohod.geohodbackend.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;
    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reviewService = new ReviewServiceImpl(reviewRepository);
    }

    @Test
    void testSubmitReview() {
        UUID authorId = UUID.randomUUID();
        ReviewCreateRequest request = new ReviewCreateRequest(UUID.randomUUID(), UUID.randomUUID(), 5, "Great event!");
        Review review = new Review(request.eventId(), authorId, request.targetUserId(), request.rating(), request.comment());
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        Review result = reviewService.submitReview(authorId, request);
        assertEquals(request.eventId(), result.getEventId());
        assertEquals(authorId, result.getAuthorId());
        assertEquals(request.targetUserId(), result.getTargetUserId());
        assertEquals(request.rating(), result.getRating());
        assertEquals(request.comment(), result.getComment());
    }

    @Test
    void testHideReview() {
        UUID reviewId = UUID.randomUUID();
        Review review = new Review();
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        reviewService.hideReview(reviewId);
        assertTrue(review.isHidden());
    }

    @Test
    void testUnhideReview() {
        UUID reviewId = UUID.randomUUID();
        Review review = new Review();
        review.hide();
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        reviewService.unhideReview(reviewId);
        assertFalse(review.isHidden());
    }
} 