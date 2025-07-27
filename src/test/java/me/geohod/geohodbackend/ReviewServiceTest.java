package me.geohod.geohodbackend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import me.geohod.geohodbackend.api.dto.review.ReviewCreateRequest;
import me.geohod.geohodbackend.data.dto.ReviewWithAuthorDto;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.data.model.repository.ReviewRepository;
import me.geohod.geohodbackend.data.model.review.Review;
import me.geohod.geohodbackend.service.IUserRatingService;
import me.geohod.geohodbackend.service.impl.ReviewServiceImpl;

public class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private IUserRatingService userRatingService;
    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reviewService = new ReviewServiceImpl(reviewRepository, eventRepository, userRatingService);
    }

    @Test
    void testSubmitReview() {
        UUID authorId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID eventAuthorId = UUID.randomUUID();
        ReviewCreateRequest request = new ReviewCreateRequest(eventId, 5, "Great event!");
        
        Event event = new Event("Test Event", "Test Description", Instant.now(), 10, eventAuthorId);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        
        Review review = new Review(request.eventId(), authorId, request.rating(), request.comment());
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        
        Review result = reviewService.submitReview(authorId, request);
        
        assertEquals(request.eventId(), result.getEventId());
        assertEquals(authorId, result.getAuthorId());
        assertEquals(request.rating(), result.getRating());
        assertEquals(request.comment(), result.getComment());
        
        // Verify that user rating update was called
        verify(userRatingService).updateUserRatingAsync(eventAuthorId);
    }

    @Test
    void testHideReview() {
        UUID reviewId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID eventAuthorId = UUID.randomUUID();
        
        Review review = new Review(eventId, eventAuthorId, 5, "Great event!");

        when(reviewRepository.findByIdAndEventAuthorId(reviewId, eventAuthorId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        
        reviewService.hideReview(reviewId, eventAuthorId);
        
        assertTrue(review.isHidden());
    }

    @Test
    void testUnhideReview() {
        UUID reviewId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID eventAuthorId = UUID.randomUUID();
        
        Review review = new Review(eventId, eventAuthorId, 5, "Great event!");
        review.hide();

        when(reviewRepository.findByIdAndEventAuthorId(reviewId, eventAuthorId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        
        reviewService.unhideReview(reviewId, eventAuthorId);
        
        assertFalse(review.isHidden());
    }

    @Test
    void testGetReviewsForUser() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        List<Review> reviews = List.of(new Review(UUID.randomUUID(), UUID.randomUUID(), 5, "Great event!"));
        
        when(reviewRepository.findByEventAuthorIdWithPaging(userId, 10, 0)).thenReturn(reviews);
        when(reviewRepository.countByEventAuthorId(userId)).thenReturn(1L);
        
        Page<Review> result = reviewService.getReviewsForUser(userId, pageable);
        
        assertEquals(1, result.getContent().size());
        assertEquals(reviews.get(0), result.getContent().get(0));
        assertEquals(1L, result.getTotalElements());
        verify(reviewRepository).findByEventAuthorIdWithPaging(userId, 10, 0);
        verify(reviewRepository).countByEventAuthorId(userId);
    }

    @Test
    void testGetReviewsWithAuthorForUser_OwnReviews_ShowHidden() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        ReviewRepository.ReviewWithAuthorProjection projection = new ReviewRepository.ReviewWithAuthorProjection(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "testuser",
            "image.jpg",
            5,
            "Great event!",
            true,
            Instant.now()
        );
        List<ReviewRepository.ReviewWithAuthorProjection> projections = List.of(projection);
        when(reviewRepository.findReviewsWithAuthorForUser(userId, true, 10, 0)).thenReturn(projections);
        when(reviewRepository.countReviewsWithAuthorForUser(userId, true)).thenReturn(1L);

        Page<ReviewWithAuthorDto> result = reviewService.getReviewsWithAuthorForUser(userId, userId, pageable);
        assertEquals(1, result.getContent().size());
        assertEquals("testuser", result.getContent().get(0).authorUsername());
        assertTrue(result.getContent().get(0).isHidden());
        verify(reviewRepository).findReviewsWithAuthorForUser(userId, true, 10, 0);
        verify(reviewRepository).countReviewsWithAuthorForUser(userId, true);
    }

    @Test
    void testGetReviewsWithAuthorForUser_OtherUser_OnlyUnhidden() {
        UUID userId = UUID.randomUUID();
        UUID requestingUserId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        ReviewRepository.ReviewWithAuthorProjection projection = new ReviewRepository.ReviewWithAuthorProjection(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "testuser",
            "image.jpg",
            5,
            "Great event!",
            false,
            Instant.now()
        );
        List<ReviewRepository.ReviewWithAuthorProjection> projections = List.of(projection);
        when(reviewRepository.findReviewsWithAuthorForUser(userId, false, 10, 0)).thenReturn(projections);
        when(reviewRepository.countReviewsWithAuthorForUser(userId, false)).thenReturn(1L);

        Page<ReviewWithAuthorDto> result = reviewService.getReviewsWithAuthorForUser(userId, requestingUserId, pageable);
        assertEquals(1, result.getContent().size());
        assertEquals("testuser", result.getContent().get(0).authorUsername());
        assertFalse(result.getContent().get(0).isHidden());
        verify(reviewRepository).findReviewsWithAuthorForUser(userId, false, 10, 0);
        verify(reviewRepository).countReviewsWithAuthorForUser(userId, false);
    }
}
