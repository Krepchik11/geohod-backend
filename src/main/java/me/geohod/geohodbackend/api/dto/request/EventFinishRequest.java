package me.geohod.geohodbackend.api.dto.request;

public record EventFinishRequest(
        boolean notifyParticipants,
        boolean sendPollLink,
        boolean sendDonationRequest
) {
}
