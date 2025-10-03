package me.geohod.geohodbackend.api.dto.response;

import java.util.Map;

public record UserStatsResponse(
    Double overallRating,
    Integer reviewsCount,
    Integer eventsCount,
    Integer eventsParticipantsCount,
    Map<Integer, Integer> reviewsByRating
) {
}