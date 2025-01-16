package me.geohod.geohodbackend.service;

import java.util.UUID;

public interface IEventNotificationService {
    void notifyParticipantsEventCancelled(UUID eventId);
    void notifyParticipantRegisteredOnEvent(UUID userId, UUID eventId);
    void notifyParticipantUnregisteredFromEvent(UUID userId, UUID eventId);
    void notifyAuthorEventCreated(UUID eventId);
}