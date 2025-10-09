package me.geohod.geohodbackend.service.impl;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.api.dto.response.UserStatsResponse;
import me.geohod.geohodbackend.data.dto.UserRatingDto;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.data.model.repository.ReviewRepository;
import me.geohod.geohodbackend.service.IUserRatingService;
import me.geohod.geohodbackend.service.IUserStatsService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStatsServiceImpl implements IUserStatsService {

    private final IUserRatingService userRatingService;
    private final EventRepository eventRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public UserStatsResponse getUserStats(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        log.debug("Calculating statistics for user: {}", userId);

        try {
            UserRatingDto rating = userRatingService.getUserRating(userId);
            Integer eventsCount = getUserEventsCount(userId);
            Integer eventsParticipantsCount = getUserEventsParticipantsCount(userId);
            Map<Integer, Integer> reviewsByRating = getUserReviewsByRating(userId);

            return new UserStatsResponse(
                rating.averageRating().doubleValue(),
                rating.totalReviewsCount(),
                eventsCount,
                eventsParticipantsCount,
                reviewsByRating
            );
        } catch (Exception e) {
            log.error("Failed to calculate statistics for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to calculate user statistics", e);
        }
    }

    @Override
    public Integer getUserEventsCount(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        long count = eventRepository.countByAuthorId(userId);
        return (int) count;
    }

    @Override
    public Integer getUserEventsParticipantsCount(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        long totalParticipants = eventRepository.sumParticipantsByAuthorId(userId);
        return (int) totalParticipants;
    }

    @Override
    public Map<Integer, Integer> getUserReviewsByRating(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        return reviewRepository.countReviewsByRatingForUser(userId)
                .stream()
                .collect(Collectors.toMap(
                    ReviewRepository.ReviewRatingCountProjection::rating,
                    projection -> (int) projection.count()
                ));
    }
}