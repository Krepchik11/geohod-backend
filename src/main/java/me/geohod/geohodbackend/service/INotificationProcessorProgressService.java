package me.geohod.geohodbackend.service;

import java.util.UUID;

public interface INotificationProcessorProgressService {
    void updateProgress(String processorName, UUID lastProcessedEventLogId);
} 