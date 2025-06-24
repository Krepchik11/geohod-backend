package me.geohod.geohodbackend.data.model.repository;

import me.geohod.geohodbackend.data.model.userrating.UserRating;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRatingRepository extends CrudRepository<UserRating, UUID> {
    Optional<UserRating> findByUserId(UUID userId);
} 