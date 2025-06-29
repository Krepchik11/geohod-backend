package me.geohod.geohodbackend;

import me.geohod.geohodbackend.api.dto.review.ReviewCreateRequest;
import me.geohod.geohodbackend.data.dto.ReviewWithAuthorDto;
import me.geohod.geohodbackend.data.model.repository.ReviewRepository;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.data.model.repository.UserRepository;
import me.geohod.geohodbackend.data.model.review.Review;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.service.IUserRatingService;
import me.geohod.geohodbackend.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private IUserRatingService userRatingService;
    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reviewService = new ReviewServiceImpl(reviewRepository, eventRepository, userRepository, userRatingService);
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

        when(reviewRepository.findByIdAndAuthorId(reviewId, eventAuthorId)).thenReturn(Optional.of(review));
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

        when(reviewRepository.findByIdAndAuthorId(reviewId, eventAuthorId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        
        reviewService.unhideReview(reviewId, eventAuthorId);
        
        assertFalse(review.isHidden());
    }

    @Test
    void testGetReviewsForUser() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        List<Review> reviews = List.of(new Review(UUID.randomUUID(), UUID.randomUUID(), 5, "Great event!"));
        Page<Review> reviewPage = new PageImpl<>(reviews, pageable, 1);
        
        when(reviewRepository.findByEventAuthorId(userId, pageable)).thenReturn(reviewPage);
        
        Page<Review> result = reviewService.getReviewsForUser(userId, pageable);
        
        assertEquals(reviewPage, result);
        verify(reviewRepository).findByEventAuthorId(userId, pageable);
    }

    @Test
    void testGetReviewsWithAuthorForUser() {
        UUID userId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        
        Review review = new Review(UUID.randomUUID(), authorId, 5, "Great event!");
        List<Review> reviews = List.of(review);
        Page<Review> reviewPage = new PageImpl<>(reviews, pageable, 1);
        
        User author = new User("tg123", "testuser", "Test", "User", "image.jpg");
        
        when(reviewRepository.findByEventAuthorId(userId, pageable)).thenReturn(reviewPage);
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        
        Page<ReviewWithAuthorDto> result = reviewService.getReviewsWithAuthorForUser(userId, pageable);
        
        assertEquals(1, result.getContent().size());
        assertEquals(author.getTgUsername(), result.getContent().get(0).authorUsername());
        assertEquals(author.getTgImageUrl(), result.getContent().get(0).authorImageUrl());
        verify(reviewRepository).findByEventAuthorId(userId, pageable);
        verify(userRepository).findById(authorId);
    }
} 