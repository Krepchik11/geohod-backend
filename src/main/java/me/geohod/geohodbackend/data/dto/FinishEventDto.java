package me.geohod.geohodbackend.data.dto;

import java.util.UUID;

public record FinishEventDto(
        UUID eventId,
        boolean sendPollLink,
        boolean sendDonationRequest
) {
}
