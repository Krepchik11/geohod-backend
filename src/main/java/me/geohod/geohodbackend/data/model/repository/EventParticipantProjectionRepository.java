package me.geohod.geohodbackend.data.model.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EventParticipantProjectionRepository {
        private final NamedParameterJdbcTemplate jdbcTemplate;

        public List<EventParticipantProjection> findEventParticipantByEventId(UUID eventId) {
                String sql = """
                                SELECT ep.id                                     AS participant_id,
                                       u.tg_username                             AS username,
                                       u.tg_id                                   AS tg_user_id,
                                       CONCAT_WS(' ', u.first_name, u.last_name) AS name,
                                       u.tg_image_url                            AS image_url,
                                       us.phone_number                           AS phone_number
                                FROM events e
                                         JOIN event_participants ep ON ep.event_id = e.id
                                         JOIN users u ON u.id = ep.user_id
                                         LEFT JOIN user_settings us ON us.user_id = u.id
                                WHERE e.id = :eventId
                                """;

                Map<String, Object> params = Map.of("eventId", eventId);

                return jdbcTemplate.query(sql, params,
                                (rs, rowNum) -> new EventParticipantProjection(
                                                rs.getString("participant_id"),
                                                rs.getString("username"),
                                                rs.getString("tg_user_id"),
                                                rs.getString("name"),
                                                rs.getString("image_url"),
                                                rs.getString("phone_number")));
        }

        public List<EventParticipantContactInfo> findEventParticipantContactInfoByEventId(UUID eventId) {
                if (eventId == null) {
                        throw new IllegalArgumentException("Event ID cannot be null");
                }

                final String sql = """
                                SELECT ep.id AS participant_id,
                                       u.tg_username AS username,
                                       us.phone_number AS phone_number
                                FROM events e
                                JOIN event_participants ep ON ep.event_id = e.id
                                JOIN users u ON u.id = ep.user_id
                                LEFT JOIN user_settings us ON us.user_id = u.id
                                WHERE e.id = :eventId
                                """;

                Map<String, Object> params = Map.of("eventId", eventId);

                return jdbcTemplate.query(sql, params,
                                (rs, rowNum) -> new EventParticipantContactInfo(
                                                rs.getString("participant_id"),
                                                rs.getString("username"),
                                                rs.getString("phone_number")));
        }

        public record EventParticipantContactInfo(
                        String id,
                        String username,
                        String phoneNumber) {
        }

        public record EventParticipantProjection(
                        String id,
                        String username,
                        String tgUserId,
                        String name,
                        String imageUrl,
                        String phoneNumber) {
        }
}