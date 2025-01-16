package me.geohod.geohodbackend.api.dto.request;

public record EventFinishRequest(
        boolean sendPollLink,
        boolean sendDonationRequest
) {
}
