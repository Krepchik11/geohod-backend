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
                SELECT
                  u.tg_username AS author_username,
                  u.first_name AS author_first_name,
                  u.last_name AS author_last_name,
                  u.tg_image_url AS author_image_url
                FROM
                  event_participants ep
                  JOIN users u ON u.id = ep.user_id
                WHERE
                  ep.event_id =:eventId
                """;

        HashMap<String, Object> params = new HashMap<>();
        params.put("eventId", eventId);

        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> new TelegramUserDetails(
                        rs.getString("author_username"),
                        rs.getString("author_first_name"),
                        rs.getString("author_last_name"),
                        rs.getString("author_image_url")
                ));
    }
}
