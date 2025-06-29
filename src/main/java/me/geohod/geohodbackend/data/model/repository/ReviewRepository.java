package me.geohod.geohodbackend.data.model.repository;

import me.geohod.geohodbackend.data.model.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends CrudRepository<Review, UUID>, PagingAndSortingRepository<Review, UUID> {
    Optional<Review> findByIdAndAuthorId(UUID eventId, UUID authorId);
    List<Review> findByEventId(UUID eventId);
    Page<Review> findByEventId(UUID eventId, Pageable pageable);
    
    @Query("SELECT r.* FROM reviews r " +
           "JOIN events e ON r.event_id = e.id " +
           "WHERE e.author_id = :userId " +
           "ORDER BY r.created_at DESC " +
           "LIMIT :limit OFFSET :offset")
    List<Review> findByEventAuthorIdWithPaging(UUID userId, int limit, int offset);
    
    @Query("SELECT COUNT(*) FROM reviews r " +
           "JOIN events e ON r.event_id = e.id " +
           "WHERE e.author_id = :userId")
    long countByEventAuthorId(UUID userId);
    
    List<Review> findByAuthorId(UUID authorId);
    
    @Query("SELECT COALESCE(AVG(r.rating), 0) as average_rating, COUNT(r.id) as total_count " +
           "FROM reviews r " +
           "JOIN events e ON r.event_id = e.id " +
           "WHERE e.author_id = :userId")
    ReviewRatingProjection calculateUserRating(UUID userId);
    
    interface ReviewRatingProjection {
        BigDecimal getAverageRating();
        Long getTotalCount();
    }
} 