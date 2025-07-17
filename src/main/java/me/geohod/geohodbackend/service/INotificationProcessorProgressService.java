package me.geohod.geohodbackend.service;

import java.time.Instant;
import java.util.UUID;

public interface INotificationProcessorProgressService {
    void updateProgress(String processorName, Instant lastProcessedCreatedAt, UUID lastProcessedId);
}
