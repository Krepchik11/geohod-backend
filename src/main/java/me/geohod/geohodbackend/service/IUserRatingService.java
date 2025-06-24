package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.data.model.userrating.UserRating;

import java.util.UUID;

public interface IUserRatingService {
    UserRating getUserRating(UUID userId);
} 