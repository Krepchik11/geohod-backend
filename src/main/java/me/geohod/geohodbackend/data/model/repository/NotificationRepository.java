package me.geohod.geohodbackend.data.model.repository;

import me.geohod.geohodbackend.data.model.notification.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, UUID> {
    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(UUID userId, boolean isRead, Pageable pageable);
    
    @Query("""
        SELECT * FROM notifications
        WHERE user_id = :userId
          AND is_read = :isRead
          AND created_at < :cursorCreatedAt
        ORDER BY created_at DESC, id DESC
        LIMIT :limit
        """)
    List<Notification> findByUserIdAndIsReadBeforeCursor(
        @Param("userId") UUID userId,
        @Param("isRead") boolean isRead,
        @Param("cursorCreatedAt") Instant cursorCreatedAt,
        @Param("limit") int limit
    );

    @Modifying
    @Query("UPDATE notifications SET is_read = true WHERE user_id = :userId AND is_read = false")
    void dismissAllByUser(@Param("userId") UUID userId);
} 