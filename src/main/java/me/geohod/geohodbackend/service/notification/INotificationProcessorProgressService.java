package me.geohod.geohodbackend.service.notification;

import java.time.Instant;
import java.util.UUID;

public interface INotificationProcessorProgressService {
    void updateProgress(String processorName, Instant lastProcessedCreatedAt, UUID lastProcessedId);
}
