package me.geohod.geohodbackend.service;

import java.util.UUID;

public interface IEventNotificationService {
    void notifyEventCancelled(UUID eventId);
}
