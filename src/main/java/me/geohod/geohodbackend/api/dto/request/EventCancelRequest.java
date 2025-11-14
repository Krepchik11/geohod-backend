package me.geohod.geohodbackend.api.dto.request;

public record EventCancelRequest(
    boolean notifyParticipants
) {
}
