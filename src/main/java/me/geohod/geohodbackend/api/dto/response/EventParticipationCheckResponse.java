package me.geohod.geohodbackend.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response indicating whether the user is participating in the event")
public record EventParticipationCheckResponse(boolean isParticipant) {
}
