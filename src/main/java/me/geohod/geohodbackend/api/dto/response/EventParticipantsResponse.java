package me.geohod.geohodbackend.api.dto.response;

import java.util.List;

public record EventParticipantsResponse(
        List<EventParticipantDetails> participants
) {
}
