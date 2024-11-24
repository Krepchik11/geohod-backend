package me.geohod.geohodbackend.data.dto;

import java.util.UUID;

public record FinishEventDto(
        UUID eventId,
        boolean notifyParticipants,
        boolean sendPollLink,
        boolean sendDonationRequest
) {
}
