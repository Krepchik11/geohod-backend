package me.geohod.geohodbackend.service.notification;

import java.util.UUID;

public interface IEventNotificationService {
    @Deprecated(since = "2.0", forRemoval = true)
    void notifyParticipantsEventCancelled(UUID eventId);

    @Deprecated(since = "2.0", forRemoval = true)
    void notifyParticipantRegisteredOnEvent(UUID userId, UUID eventId);

    @Deprecated(since = "2.0", forRemoval = true)
    void notifyParticipantUnregisteredFromEvent(UUID userId, UUID eventId);

    @Deprecated(since = "2.0", forRemoval = true)
    void notifyAuthorEventCreated(UUID eventId);

    @Deprecated(since = "2.0", forRemoval = true)
    void notifyParticipantsEventFinishedWithDonation(UUID eventId, String donationAmount);
}