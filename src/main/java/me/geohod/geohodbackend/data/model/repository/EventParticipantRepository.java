package me.geohod.geohodbackend.data.model.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import me.geohod.geohodbackend.data.model.EventParticipant;

@Repository
public interface EventParticipantRepository extends CrudRepository<EventParticipant, UUID> {
    Optional<EventParticipant> findByEventIdAndId(UUID eventId, UUID id);

    Optional<EventParticipant> findByEventIdAndUserId(UUID eventId, UUID userId);

    List<EventParticipant> findEventParticipantByEventId(UUID eventId);

    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);

    @Modifying
    @Query("DELETE FROM event_participants WHERE event_id = :eventId AND user_id = :userId")
    int deleteByEventIdAndUserId(@Param("eventId") UUID eventId, @Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE event_participants SET poll_link_sent = :pollLinkSent, cash_donated = :cashDonated, transfer_donated = :transferDonated WHERE event_id = :eventId AND user_id = :userId")
    int updateStateByEventIdAndUserId(@Param("eventId") UUID eventId,
            @Param("userId") UUID userId,
            @Param("pollLinkSent") boolean pollLinkSent,
            @Param("cashDonated") boolean cashDonated,
            @Param("transferDonated") boolean transferDonated);
}
