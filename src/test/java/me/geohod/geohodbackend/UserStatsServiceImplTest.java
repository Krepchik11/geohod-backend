package me.geohod.geohodbackend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.geohod.geohodbackend.api.dto.response.UserStatsResponse;
import me.geohod.geohodbackend.data.dto.UserRatingDto;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.data.model.repository.ReviewRepository;
import me.geohod.geohodbackend.service.IUserRatingService;
import me.geohod.geohodbackend.service.impl.UserStatsServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserStatsServiceImplTest {

    @Mock
    private IUserRatingService userRatingService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ReviewRepository reviewRepository;

    private UserStatsServiceImpl userStatsService;

    private UUID testUserId;
    private UserRatingDto testUserRating;

    @BeforeEach
    void setUp() {
        userStatsService = new UserStatsServiceImpl(userRatingService, eventRepository, reviewRepository);
        testUserId = UUID.randomUUID();
        testUserRating = new UserRatingDto(testUserId, new BigDecimal("4.5"), 10);
    }

    @Test
    void testGetUserStats_Success() {
        when(userRatingService.getUserRating(testUserId)).thenReturn(testUserRating);
        when(eventRepository.countByAuthorId(testUserId)).thenReturn(5L);
        when(eventRepository.sumParticipantsByAuthorId(testUserId)).thenReturn(25L);

        ReviewRepository.ReviewRatingCountProjection projection1 =
            new ReviewRepository.ReviewRatingCountProjection(5, 3L);
        ReviewRepository.ReviewRatingCountProjection projection2 =
            new ReviewRepository.ReviewRatingCountProjection(4, 2L);
        when(reviewRepository.countReviewsByRatingForUser(testUserId))
            .thenReturn(List.of(projection1, projection2));

        UserStatsResponse result = userStatsService.getUserStats(testUserId);

        assertNotNull(result);
        assertEquals(4.5, result.overallRating());
        assertEquals(10, result.reviewsCount());
        assertEquals(5, result.eventsCount());
        assertEquals(25, result.eventsParticipantsCount());
        assertEquals(5, result.reviewsByRating().size());
        assertEquals(3, result.reviewsByRating().get(5));
        assertEquals(2, result.reviewsByRating().get(4));
        assertEquals(0, result.reviewsByRating().get(3));
        assertEquals(0, result.reviewsByRating().get(2));
        assertEquals(0, result.reviewsByRating().get(1));

        verify(userRatingService).getUserRating(testUserId);
        verify(eventRepository).countByAuthorId(testUserId);
        verify(eventRepository).sumParticipantsByAuthorId(testUserId);
        verify(reviewRepository).countReviewsByRatingForUser(testUserId);
    }

    @Test
    void testGetUserStats_NullUserId_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userStatsService.getUserStats(null)
        );

        assertEquals("User ID cannot be null", exception.getMessage());

        verifyNoInteractions(userRatingService);
        verifyNoInteractions(eventRepository);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void testGetUserStats_ServiceException_ThrowsRuntimeException() {
        when(userRatingService.getUserRating(testUserId))
            .thenThrow(new RuntimeException("Database connection failed"));

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userStatsService.getUserStats(testUserId)
        );

        assertEquals("Failed to calculate user statistics", exception.getMessage());
        assertEquals("Database connection failed", exception.getCause().getMessage());
    }

    @Test
    void testGetUserStats_ZeroValues() {
        UserRatingDto zeroRating = new UserRatingDto(testUserId, BigDecimal.ZERO, 0);
        when(userRatingService.getUserRating(testUserId)).thenReturn(zeroRating);
        when(eventRepository.countByAuthorId(testUserId)).thenReturn(0L);
        when(eventRepository.sumParticipantsByAuthorId(testUserId)).thenReturn(0L);
        when(reviewRepository.countReviewsByRatingForUser(testUserId))
            .thenReturn(List.of());

        UserStatsResponse result = userStatsService.getUserStats(testUserId);

        assertNotNull(result);
        assertEquals(0.0, result.overallRating());
        assertEquals(0, result.reviewsCount());
        assertEquals(0, result.eventsCount());
        assertEquals(0, result.eventsParticipantsCount());
        assertEquals(5, result.reviewsByRating().size());
        assertEquals(0, result.reviewsByRating().get(1));
        assertEquals(0, result.reviewsByRating().get(2));
        assertEquals(0, result.reviewsByRating().get(3));
        assertEquals(0, result.reviewsByRating().get(4));
        assertEquals(0, result.reviewsByRating().get(5));
    }

    @Test
    void testGetUserEventsCount_Success() {
        when(eventRepository.countByAuthorId(testUserId)).thenReturn(7L);

        Integer result = userStatsService.getUserEventsCount(testUserId);

        assertEquals(7, result);
        verify(eventRepository).countByAuthorId(testUserId);
    }

    @Test
    void testGetUserEventsCount_NullUserId_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userStatsService.getUserEventsCount(null)
        );

        assertEquals("User ID cannot be null", exception.getMessage());
        verifyNoInteractions(eventRepository);
    }

    @Test
    void testGetUserEventsCount_NoEvents_ReturnsZero() {
        when(eventRepository.countByAuthorId(testUserId)).thenReturn(0L);

        Integer result = userStatsService.getUserEventsCount(testUserId);

        assertEquals(0, result);
    }

    @Test
    void testGetUserEventsParticipantsCount_Success() {
        when(eventRepository.sumParticipantsByAuthorId(testUserId)).thenReturn(42L);

        Integer result = userStatsService.getUserEventsParticipantsCount(testUserId);

        assertEquals(42, result);
        verify(eventRepository).sumParticipantsByAuthorId(testUserId);
    }

    @Test
    void testGetUserEventsParticipantsCount_NullUserId_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userStatsService.getUserEventsParticipantsCount(null)
        );

        assertEquals("User ID cannot be null", exception.getMessage());
        verifyNoInteractions(eventRepository);
    }

    @Test
    void testGetUserEventsParticipantsCount_NoParticipants_ReturnsZero() {
        when(eventRepository.sumParticipantsByAuthorId(testUserId)).thenReturn(0L);

        Integer result = userStatsService.getUserEventsParticipantsCount(testUserId);

        assertEquals(0, result);
    }

    @Test
    void testGetUserReviewsByRating_Success() {
        ReviewRepository.ReviewRatingCountProjection projection1 =
            new ReviewRepository.ReviewRatingCountProjection(5, 5L);
        ReviewRepository.ReviewRatingCountProjection projection2 =
            new ReviewRepository.ReviewRatingCountProjection(4, 3L);
        ReviewRepository.ReviewRatingCountProjection projection3 =
            new ReviewRepository.ReviewRatingCountProjection(3, 1L);

        when(reviewRepository.countReviewsByRatingForUser(testUserId))
            .thenReturn(List.of(projection1, projection2, projection3));

        Map<Integer, Integer> result = userStatsService.getUserReviewsByRating(testUserId);

        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals(5, result.get(5));
        assertEquals(3, result.get(4));
        assertEquals(1, result.get(3));
        assertEquals(0, result.get(2));
        assertEquals(0, result.get(1));

        verify(reviewRepository).countReviewsByRatingForUser(testUserId);
    }

    @Test
    void testGetUserReviewsByRating_NullUserId_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userStatsService.getUserReviewsByRating(null)
        );

        assertEquals("User ID cannot be null", exception.getMessage());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void testGetUserReviewsByRating_NoReviews_ReturnsAllCategoriesWithZeroCounts() {
        when(reviewRepository.countReviewsByRatingForUser(testUserId))
            .thenReturn(List.of());

        Map<Integer, Integer> result = userStatsService.getUserReviewsByRating(testUserId);

        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals(0, result.get(1));
        assertEquals(0, result.get(2));
        assertEquals(0, result.get(3));
        assertEquals(0, result.get(4));
        assertEquals(0, result.get(5));
    }

    @Test
    void testGetUserReviewsByRating_SingleRating() {
        ReviewRepository.ReviewRatingCountProjection projection =
            new ReviewRepository.ReviewRatingCountProjection(5, 8L);

        when(reviewRepository.countReviewsByRatingForUser(testUserId))
            .thenReturn(List.of(projection));

        Map<Integer, Integer> result = userStatsService.getUserReviewsByRating(testUserId);

        assertEquals(5, result.size());
        assertEquals(8, result.get(5));
        assertEquals(0, result.get(4));
        assertEquals(0, result.get(3));
        assertEquals(0, result.get(2));
        assertEquals(0, result.get(1));
    }

    @Test
    void testGetUserStats_IntegrationWithVariousData() {
        UserRatingDto differentRating = new UserRatingDto(testUserId, new BigDecimal("3.7"), 15);
        when(userRatingService.getUserRating(testUserId)).thenReturn(differentRating);
        when(eventRepository.countByAuthorId(testUserId)).thenReturn(3L);
        when(eventRepository.sumParticipantsByAuthorId(testUserId)).thenReturn(12L);

        ReviewRepository.ReviewRatingCountProjection fiveStar =
            new ReviewRepository.ReviewRatingCountProjection(5, 10L);
        ReviewRepository.ReviewRatingCountProjection oneStar =
            new ReviewRepository.ReviewRatingCountProjection(1, 5L);
        when(reviewRepository.countReviewsByRatingForUser(testUserId))
            .thenReturn(List.of(fiveStar, oneStar));

        UserStatsResponse result = userStatsService.getUserStats(testUserId);

        assertEquals(3.7, result.overallRating());
        assertEquals(15, result.reviewsCount());
        assertEquals(3, result.eventsCount());
        assertEquals(12, result.eventsParticipantsCount());
        assertEquals(5, result.reviewsByRating().size());
        assertEquals(10, result.reviewsByRating().get(5));
        assertEquals(5, result.reviewsByRating().get(1));
        assertEquals(0, result.reviewsByRating().get(4));
        assertEquals(0, result.reviewsByRating().get(3));
        assertEquals(0, result.reviewsByRating().get(2));
    }

    @Test
    void testGetUserReviewsByRating_MiddleRatingsOnly_AllCategoriesReturned() {
        // Test with only 2, 3, 4 star ratings - should fill in 1 and 5 with 0
        ReviewRepository.ReviewRatingCountProjection twoStar =
            new ReviewRepository.ReviewRatingCountProjection(2, 3L);
        ReviewRepository.ReviewRatingCountProjection threeStar =
            new ReviewRepository.ReviewRatingCountProjection(3, 5L);
        ReviewRepository.ReviewRatingCountProjection fourStar =
            new ReviewRepository.ReviewRatingCountProjection(4, 2L);

        when(reviewRepository.countReviewsByRatingForUser(testUserId))
            .thenReturn(List.of(twoStar, threeStar, fourStar));

        Map<Integer, Integer> result = userStatsService.getUserReviewsByRating(testUserId);

        assertEquals(5, result.size());
        assertEquals(0, result.get(1)); // Missing rating gets 0
        assertEquals(3, result.get(2)); // Existing rating
        assertEquals(5, result.get(3)); // Existing rating
        assertEquals(2, result.get(4)); // Existing rating
        assertEquals(0, result.get(5)); // Missing rating gets 0
    }

    @Test
    void testGetUserReviewsByRating_EvenRatingsOnly_AllCategoriesReturned() {
        // Test with only 2 and 4 star ratings - should fill in 1, 3, 5 with 0
        ReviewRepository.ReviewRatingCountProjection twoStar =
            new ReviewRepository.ReviewRatingCountProjection(2, 4L);
        ReviewRepository.ReviewRatingCountProjection fourStar =
            new ReviewRepository.ReviewRatingCountProjection(4, 6L);

        when(reviewRepository.countReviewsByRatingForUser(testUserId))
            .thenReturn(List.of(twoStar, fourStar));

        Map<Integer, Integer> result = userStatsService.getUserReviewsByRating(testUserId);

        assertEquals(5, result.size());
        assertEquals(0, result.get(1)); // Missing rating gets 0
        assertEquals(4, result.get(2)); // Existing rating
        assertEquals(0, result.get(3)); // Missing rating gets 0
        assertEquals(6, result.get(4)); // Existing rating
        assertEquals(0, result.get(5)); // Missing rating gets 0
    }

    @Test
    void testGetUserReviewsByRating_OddRatingsOnly_AllCategoriesReturned() {
        // Test with only 1, 3, 5 star ratings - should fill in 2 and 4 with 0
        ReviewRepository.ReviewRatingCountProjection oneStar =
            new ReviewRepository.ReviewRatingCountProjection(1, 2L);
        ReviewRepository.ReviewRatingCountProjection threeStar =
            new ReviewRepository.ReviewRatingCountProjection(3, 7L);
        ReviewRepository.ReviewRatingCountProjection fiveStar =
            new ReviewRepository.ReviewRatingCountProjection(5, 1L);

        when(reviewRepository.countReviewsByRatingForUser(testUserId))
            .thenReturn(List.of(oneStar, threeStar, fiveStar));

        Map<Integer, Integer> result = userStatsService.getUserReviewsByRating(testUserId);

        assertEquals(5, result.size());
        assertEquals(2, result.get(1)); // Existing rating
        assertEquals(0, result.get(2)); // Missing rating gets 0
        assertEquals(7, result.get(3)); // Existing rating
        assertEquals(0, result.get(4)); // Missing rating gets 0
        assertEquals(1, result.get(5)); // Existing rating
    }

    @Test
    void testGetUserReviewsByRating_AllRatingsPresent_NoZerosAdded() {
        // Test with all ratings 1-5 present - should return all as-is
        ReviewRepository.ReviewRatingCountProjection oneStar =
            new ReviewRepository.ReviewRatingCountProjection(1, 1L);
        ReviewRepository.ReviewRatingCountProjection twoStar =
            new ReviewRepository.ReviewRatingCountProjection(2, 2L);
        ReviewRepository.ReviewRatingCountProjection threeStar =
            new ReviewRepository.ReviewRatingCountProjection(3, 3L);
        ReviewRepository.ReviewRatingCountProjection fourStar =
            new ReviewRepository.ReviewRatingCountProjection(4, 4L);
        ReviewRepository.ReviewRatingCountProjection fiveStar =
            new ReviewRepository.ReviewRatingCountProjection(5, 5L);

        when(reviewRepository.countReviewsByRatingForUser(testUserId))
            .thenReturn(List.of(oneStar, twoStar, threeStar, fourStar, fiveStar));

        Map<Integer, Integer> result = userStatsService.getUserReviewsByRating(testUserId);

        assertEquals(5, result.size());
        assertEquals(1, result.get(1));
        assertEquals(2, result.get(2));
        assertEquals(3, result.get(3));
        assertEquals(4, result.get(4));
        assertEquals(5, result.get(5));
    }
}