package me.geohod.geohodbackend.data.model.repository;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.dto.TelegramUserDetails;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ParticipantProjectionRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<TelegramUserDetails> participants(UUID eventId) {
        String sql = """
                SELECT u.tg_username as author_username,
                u.tg_name as author_name,
                u.tg_image_url as author_image_url
                FROM event_participants ep
                JOIN users u on u.id = ep.user_id
                WHERE ep.event_id = :eventId
                """;

        HashMap<String, Object> params = new HashMap<>();
        params.put("eventId", eventId);

        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> new TelegramUserDetails(
                        rs.getString("author_username"),
                        rs.getString("author_name"),
                        rs.getString("author_image_url")
                ));
    }
}
