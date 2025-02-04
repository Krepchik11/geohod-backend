package me.geohod.geohodbackend.data.model.repository;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.dto.EventParticipantProjection;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
                       u.tg_image_url                            AS image_url
                FROM events e
                         JOIN event_participants ep ON ep.event_id = e.id
                         JOIN users u ON u.id = ep.user_id
                WHERE e.id = :eventId
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("eventId", eventId);

        return jdbcTemplate.query(sql, params,
                (rs, rowNum) -> new EventParticipantProjection(
                        rs.getString("participant_id"),
                        rs.getString("username"),
                        rs.getString("tg_user_id"),
                        rs.getString("name"),
                        rs.getString("image_url")
                ));
    }
}