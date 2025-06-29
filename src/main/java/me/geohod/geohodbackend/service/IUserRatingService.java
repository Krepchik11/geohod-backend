package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.data.dto.UserRatingDto;

import java.util.UUID;

public interface IUserRatingService {
    UserRatingDto getUserRating(UUID userId);
    
    void updateUserRating(UUID userId);
    
    void updateUserRatingAsync(UUID userId);
} 