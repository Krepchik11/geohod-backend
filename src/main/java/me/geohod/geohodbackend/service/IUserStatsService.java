package me.geohod.geohodbackend.service;

import java.util.Map;
import java.util.UUID;

import me.geohod.geohodbackend.api.dto.response.UserStatsResponse;

public interface IUserStatsService {
    UserStatsResponse getUserStats(UUID userId);

    Integer getUserEventsCount(UUID userId);

    Integer getUserEventsParticipantsCount(UUID userId);

    Map<Integer, Integer> getUserReviewsByRating(UUID userId);
}