package me.geohod.geohodbackend.api.dto.request;

public record EventFinishRequest(
        boolean sendPollLink,
        boolean donationCash,
        boolean donationTransfer,

        @Deprecated
        boolean sendDonationRequest,
        @Deprecated
        String donationInfo
) {
}
