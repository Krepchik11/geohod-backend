package me.geohod.geohodbackend.data.model.repository;

import me.geohod.geohodbackend.data.model.review.Review;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends CrudRepository<Review, UUID> {
} 