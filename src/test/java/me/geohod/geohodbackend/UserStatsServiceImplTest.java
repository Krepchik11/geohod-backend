package me.geohod.geohodbackend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        assertEquals(2, result.reviewsByRating().size());
        assertEquals(3, result.reviewsByRating().get(5));
        assertEquals(2, result.reviewsByRating().get(4));

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
        assertTrue(result.reviewsByRating().isEmpty());
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
        assertEquals(3, result.size());
        assertEquals(5, result.get(5));
        assertEquals(3, result.get(4));
        assertEquals(1, result.get(3));

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
    void testGetUserReviewsByRating_NoReviews_ReturnsEmptyMap() {
        when(reviewRepository.countReviewsByRatingForUser(testUserId))
            .thenReturn(List.of());

        Map<Integer, Integer> result = userStatsService.getUserReviewsByRating(testUserId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetUserReviewsByRating_SingleRating() {
        ReviewRepository.ReviewRatingCountProjection projection =
            new ReviewRepository.ReviewRatingCountProjection(5, 8L);

        when(reviewRepository.countReviewsByRatingForUser(testUserId))
            .thenReturn(List.of(projection));

        Map<Integer, Integer> result = userStatsService.getUserReviewsByRating(testUserId);

        assertEquals(1, result.size());
        assertEquals(8, result.get(5));
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
        assertEquals(2, result.reviewsByRating().size());
        assertEquals(10, result.reviewsByRating().get(5));
        assertEquals(5, result.reviewsByRating().get(1));
    }
}