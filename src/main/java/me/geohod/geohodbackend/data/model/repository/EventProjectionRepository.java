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

    public EventDetailedProjection event(UUID eventId, UUID userId) {
        String sql = """
                    SELECT
                        e.id AS event_id,
                        u.tg_id AS author_tg_id,
                        u.tg_username AS author_username,
                        u.first_name AS author_first_name,
                        u.last_name AS author_last_name,
                        u.tg_image_url AS author_image_url,
                        us.phone_number AS author_phone_number,
                        e.name AS event_name,
                        e.description AS event_description,
                        e.date AS event_date,
                        e.max_participants AS event_max_participants,
                        e.current_participants AS event_current_participants,
                        e.status AS event_status,
                        e.send_poll_link,
                        e.donation_cash,
                        e.donation_transfer,
                        BOOL_OR(p.poll_link_sent) as poll_link_sent,
                        BOOL_OR(p.cash_donated) as cash_donated,
                        BOOL_OR(p.transfer_donated) as transfer_donated
                    FROM
                        events e
                    JOIN
                        users u ON e.author_id = u.id
                    LEFT JOIN
                        user_settings us ON us.user_id = u.id
                    LEFT JOIN
                        event_participants p ON e.id = p.event_id AND p.user_id = :userId
                    WHERE
                        e.id = :eventId
                    GROUP BY
                        e.id, u.id, us.phone_number
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("eventId", eventId);
        params.put("userId", userId);

        return jdbcTemplate.queryForObject(
                sql,
                params,
                (ResultSet rs, int _) -> new EventDetailedProjection(
                        UUID.fromString(rs.getString("event_id")),
                        new TelegramUserDetails(
                                rs.getString("author_tg_id"),
                                rs.getString("author_username"),
                                rs.getString("author_first_name"),
                                rs.getString("author_last_name"),
                                rs.getString("author_image_url"),
                                rs.getString("author_phone_number")),
                        rs.getString("event_name"),
                        rs.getString("event_description"),
                        rs.getTimestamp("event_date").toInstant(),
                        rs.getInt("event_max_participants"),
                        rs.getInt("event_current_participants"),
                        Event.Status.valueOf(rs.getString("event_status")),
                        rs.getBoolean("send_poll_link"),
                        rs.getBoolean("donation_cash"),
                        rs.getBoolean("donation_transfer"),
                        new EventDetailedProjection.ParticipantState(
                                rs.getBoolean("poll_link_sent"),
                                rs.getBoolean("cash_donated"),
                                rs.getBoolean("transfer_donated"))));
    }

    public Page<EventDetailedProjection> events(
            UUID authorUserId,
            UUID participantUserId,
            List<Event.Status> statuses,
            Pageable pageable) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (authorUserId != null) {
            whereClause.append(" AND e.author_id = :authorUserId ");
            params.put("authorUserId", authorUserId);
        }

        if (participantUserId != null) {
            if (authorUserId != null) {
                whereClause = new StringBuilder(
                        " WHERE (e.author_id = :authorUserId OR p.user_id = :participantUserId) ");
                params.put("participantUserId", participantUserId);
            } else {
                whereClause.append(" AND p.user_id = :participantUserId ");
                params.put("participantUserId", participantUserId);
            }
        }

        List<String> statusesFilter = prepareStatusesFilter(statuses);
        whereClause.append(" AND e.status IN (:statuses) ");
        params.put("statuses", statusesFilter);

        String baseSql = """
                    FROM events e
                        JOIN users u ON e.author_id = u.id
                        LEFT JOIN user_settings us ON us.user_id = u.id
                        LEFT JOIN event_participants p ON e.id = p.event_id
                """;

        String countSql = "SELECT COUNT(DISTINCT e.id) " + baseSql + whereClause;

        String orderByClause = buildOrderByClause(pageable);

        String selectSql = """
                    SELECT DISTINCT
                        e.id AS event_id,
                        e.created_at AS created_at,
                        u.tg_id AS author_tg_id,
                        u.tg_username AS author_username,
                        u.first_name AS author_first_name,
                        u.last_name AS author_last_name,
                        u.tg_image_url AS author_image_url,
                        us.phone_number AS author_phone_number,
                        e.name AS event_name,
                        e.description AS event_description,
                        e.date AS event_date,
                        e.max_participants AS event_max_participants,
                        e.current_participants AS event_current_participants,
                        e.status AS event_status,
                        e.send_poll_link,
                        e.donation_cash,
                        e.donation_transfer,
                        BOOL_OR(p.poll_link_sent) as poll_link_sent,
                        BOOL_OR(p.cash_donated) as cash_donated,
                        BOOL_OR(p.transfer_donated) as transfer_donated
                """ + baseSql + whereClause + " GROUP BY e.id, u.id, us.phone_number ORDER BY " + orderByClause
                + " OFFSET :offset LIMIT :pageSize";

        params.put("offset", pageable.getOffset());
        params.put("pageSize", pageable.getPageSize());

        Integer totalElements = jdbcTemplate.queryForObject(countSql, params, Integer.class);
        List<EventDetailedProjection> events = jdbcTemplate.query(
                selectSql,
                params,
                (ResultSet rs, int _) -> new EventDetailedProjection(
                        UUID.fromString(rs.getString("event_id")),
                        new TelegramUserDetails(
                                rs.getString("author_tg_id"),
                                rs.getString("author_username"),
                                rs.getString("author_first_name"),
                                rs.getString("author_last_name"),
                                rs.getString("author_image_url"),
                                rs.getString("author_phone_number")),
                        rs.getString("event_name"),
                        rs.getString("event_description"),
                        rs.getTimestamp("event_date").toInstant(),
                        rs.getInt("event_max_participants"),
                        rs.getInt("event_current_participants"),
                        Event.Status.valueOf(rs.getString("event_status")),
                        rs.getBoolean("send_poll_link"),
                        rs.getBoolean("donation_cash"),
                        rs.getBoolean("donation_transfer"),
                        new EventDetailedProjection.ParticipantState(
                                rs.getBoolean("poll_link_sent"),
                                rs.getBoolean("cash_donated"),
                                rs.getBoolean("transfer_donated"))));

        return new PageImpl<>(events, pageable, totalElements == null ? 0 : totalElements);
    }

    private String buildOrderByClause(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return "e.created_at DESC";
        }

        List<String> orders = new ArrayList<>();
        pageable.getSort().forEach(order -> {
            String property = order.getProperty();
            String direction = order.getDirection().name();
            String column = switch (property) {
                case "name" -> "e.name";
                case "date" -> "e.date";
                case "status" -> "e.status";
                case "createdAt" -> "e.created_at";
                case "updatedAt" -> "e.updated_at";
                default -> "e.created_at"; // Fallback to safe default
            };
            orders.add(column + " " + direction);
        });

        return String.join(", ", orders);
    }

    private List<String> prepareStatusesFilter(List<Event.Status> statuses) {
        return (statuses == null || statuses.isEmpty())
                ? Arrays.stream(Event.Status.values()).map(Enum::name).toList()
                : statuses.stream().map(Enum::name).toList();
    }
}
