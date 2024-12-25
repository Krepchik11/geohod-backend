package me.geohod.geohodbackend.service;

import java.util.UUID;

public interface IEventManager {
    void cancelEvent(UUID eventId);
}
