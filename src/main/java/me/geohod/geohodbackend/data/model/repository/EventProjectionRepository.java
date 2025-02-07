package me.geohod.geohodbackend.data.model.repository;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.dto.EventDetailedProjection;
import me.geohod.geohodbackend.data.dto.TelegramUserDetails;
import me.geohod.geohodbackend.data.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class EventProjectionRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public EventDetailedProjection event(UUID eventId) {
        String sql = """
                    SELECT
                        e.id AS event_id,
                        u.tg_id AS author_tg_id,
                        u.tg_username AS author_username,
                        u.first_name AS author_first_name,
                        u.last_name AS author_last_name,
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
                (ResultSet rs, int _) -> new EventDetailedProjection(
                        UUID.fromString(rs.getString("event_id")),
                        new TelegramUserDetails(
                                rs.getString("author_tg_id"),
                                rs.getString("author_username"),
                                rs.getString("author_first_name"),
                                rs.getString("author_last_name"),
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

    public Page<EventDetailedProjection> events(
            UUID authorUserId,
            UUID participantUserId,
            List<Event.Status> statuses,
            Pageable pageable
    ) {
        String sql = """
                    SELECT DISTINCT
                        e.id AS event_id,
                        u.tg_id AS author_tg_id,
                        u.tg_username AS author_username,
                        u.first_name AS author_first_name,
                        u.last_name AS author_last_name,
                        u.tg_image_url AS author_image_url,
                        e.name AS event_name,
                        e.description AS event_description,
                        e.date AS event_date,
                        e.max_participants AS event_max_participants,
                        e.current_participants AS event_current_participants,
                        e.status AS event_status
                    FROM events e
                        JOIN users u ON e.author_id = u.id
                        LEFT JOIN event_participants p ON e.id = p.event_id AND p.user_id = :participantUserId
                    WHERE (COALESCE(:authorUserId) IS NOT NULL AND e.author_id = :authorUserId)
                        OR (COALESCE(:participantUserId) IS NOT NULL AND p.user_id = :participantUserId)
                        AND e.status IN (:statuses)
                    ORDER BY e.created_at DESC
                    OFFSET :offset
                    LIMIT :pageSize;
                """;

        String countSql = """
                    SELECT COUNT(DISTINCT e.id)
                    FROM events e
                        JOIN users u ON e.author_id = u.id
                        LEFT JOIN event_participants p ON e.id = p.event_id AND p.user_id = :participantUserId
                    WHERE (COALESCE(:authorUserId) IS NOT NULL AND e.author_id = :authorUserId)
                        OR (COALESCE(:participantUserId) IS NOT NULL AND p.user_id = :participantUserId)
                        AND e.status IN (:statuses);
                """;

        List<String> statusesFilter = prepareStatusesFilter(statuses);

        Map<String, Object> params = new HashMap<>();
        params.put("authorUserId", authorUserId);
        params.put("participantUserId", participantUserId);
        params.put("statuses", statusesFilter);
        params.put("offset", pageable.getOffset());
        params.put("pageSize", pageable.getPageSize());

        Integer totalElements = jdbcTemplate.queryForObject(countSql, params, Integer.class);
        List<EventDetailedProjection> events = jdbcTemplate.query(
                sql,
                params,
                (ResultSet rs, int _) -> new EventDetailedProjection(
                        UUID.fromString(rs.getString("event_id")),
                        new TelegramUserDetails(
                                rs.getString("author_tg_id"),
                                rs.getString("author_username"),
                                rs.getString("author_first_name"),
                                rs.getString("author_last_name"),
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

        return new PageImpl<>(events, pageable, totalElements == null ? 0 : totalElements);
    }

    private List<String> prepareStatusesFilter(List<Event.Status> statuses) {
        return (statuses == null || statuses.isEmpty())
                ? Arrays.stream(Event.Status.values()).map(Enum::name).toList()
                : statuses.stream().map(Enum::name).toList();
    }
}
