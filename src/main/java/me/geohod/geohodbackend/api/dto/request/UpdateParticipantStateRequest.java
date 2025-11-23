package me.geohod.geohodbackend.api.dto.request;

public record UpdateParticipantStateRequest(
        boolean pollLinkSent,
        boolean cashDonated,
        boolean transferDonated) {
}
