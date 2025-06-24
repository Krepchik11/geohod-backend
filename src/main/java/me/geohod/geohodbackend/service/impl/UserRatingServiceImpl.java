package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.repository.UserRatingRepository;
import me.geohod.geohodbackend.data.model.userrating.UserRating;
import me.geohod.geohodbackend.service.IUserRatingService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserRatingServiceImpl implements IUserRatingService {

    private final UserRatingRepository userRatingRepository;

    @Override
    public UserRating getUserRating(UUID userId) {
        return userRatingRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User rating not found for user: " + userId));
    }
} 