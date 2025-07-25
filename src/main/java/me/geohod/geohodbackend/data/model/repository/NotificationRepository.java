package me.geohod.geohodbackend.data.model.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import me.geohod.geohodbackend.data.model.notification.Notification;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Long> {
    Optional<Notification> findByIdAndUserId(Long id, UUID userId);
    List<Notification> findByUserIdAndIsReadOrderByIdDesc(UUID userId, boolean isRead, Pageable pageable);
    List<Notification> findByUserIdOrderByIdDesc(UUID userId, Pageable pageable);
    
    @Query("""
        SELECT * FROM notifications
        WHERE user_id = :userId
          AND is_read = :isRead
          AND id > :cursorId
        ORDER BY id DESC
        LIMIT :limit
        """)
    List<Notification> findByUserIdAndIsReadAfterCursor(
        @Param("userId") UUID userId,
        @Param("isRead") boolean isRead,
        @Param("cursorId") Long afterId,
        @Param("limit") int limit
    );

    @Query("""
        SELECT * FROM notifications
        WHERE user_id = :userId
          AND id > :cursorId
        ORDER BY id DESC
        LIMIT :limit
        """)
    List<Notification> findByUserIdAfterCursor(
        @Param("userId") UUID userId,
        @Param("cursorId") Long afterId,
        @Param("limit") int limit
    );

    @Modifying
    @Query("UPDATE notifications SET is_read = true WHERE user_id = :userId AND is_read = false")
    void dismissAllByUser(@Param("userId") UUID userId);
}
