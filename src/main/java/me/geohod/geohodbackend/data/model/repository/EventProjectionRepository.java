package me.geohod.geohodbackend.data.model.repository;

import me.geohod.geohodbackend.data.dto.EventDetailedProjection;
import me.geohod.geohodbackend.data.dto.TelegramUserDetails;
import me.geohod.geohodbackend.data.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class EventProjectionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public EventProjectionRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public EventDetailedProjection event(UUID eventId) {
        String sql = """
                    SELECT
                        e.id AS event_id,
                        u.tg_username AS author_username,
                        u.tg_name AS author_name,
                        u.tg_image_url AS author_image_url,
                        e.name AS event_name,
                        e.description AS event_description,
                        e.date AS event_date,
                        e.max_participants AS event_max_participants,
                        e.current_participants AS event_current_participants,
                        e.status AS event_status
                    FROM
                        events e
                    JOIN
                        users u ON e.author_id = u.id
                    WHERE
                        e.id = :eventId
                """;

        return jdbcTemplate.queryForObject(
                sql,
                Map.of("eventId", eventId),
                (ResultSet rs, int rowNum) -> new EventDetailedProjection(
                        UUID.fromString(rs.getString("event_id")),
                        new TelegramUserDetails(
                                rs.getString("author_username"),
                                rs.getString("author_name"),
                                rs.getString("author_image_url")
                        ),
                        rs.getString("event_name"),
                        rs.getString("event_description"),
                        rs.getTimestamp("event_date").toInstant(),
                        rs.getInt("event_max_participants"),
                        rs.getInt("event_current_participants"),
                        Event.Status.valueOf(rs.getString("event_status"))
                )
        );
    }

    public List<EventDetailedProjection> events(UUID participantUserId, Pageable pageable) {
        String sql = """
                    SELECT
                        e.id AS event_id,
                        u.tg_username AS author_username,
                        u.tg_name AS author_name,
                        u.tg_image_url AS author_image_url,
                        e.name AS event_name,
                        e.description AS event_description,
                        e.date AS event_date,
                        e.max_participants AS event_max_participants,
                        e.current_participants AS event_current_participants,
                        e.status AS event_status
                    FROM
                        events e
                    JOIN
                        users u ON e.author_id = u.id
                    LEFT JOIN
                        event_participants ep ON e.id = ep.event_id
                    WHERE
                        (COALESCE(:participantUserId) IS NULL OR ep.user_id = :participantUserId)
                    OFFSET :offset
                    LIMIT :pageSize;
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("participantUserId", participantUserId);
        params.put("offset", pageable.getOffset());
        params.put("pageSize", pageable.getPageSize());

        return jdbcTemplate.query(
                sql,
                params,
                (ResultSet rs, int rowNum) -> new EventDetailedProjection(
                        UUID.fromString(rs.getString("event_id")),
                        new TelegramUserDetails(
                                rs.getString("author_username"),
                                rs.getString("author_name"),
                                rs.getString("author_image_url")
                        ),
                        rs.getString("event_name"),
                        rs.getString("event_description"),
                        rs.getTimestamp("event_date").toInstant(),
                        rs.getInt("event_max_participants"),
                        rs.getInt("event_current_participants"),
                        Event.Status.valueOf(rs.getString("event_status"))
                )
        );
    }
}
