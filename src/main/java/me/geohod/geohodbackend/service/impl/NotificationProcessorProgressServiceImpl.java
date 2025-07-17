package me.geohod.geohodbackend.service.impl;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.notification.NotificationProcessorProgress;
import me.geohod.geohodbackend.data.model.repository.NotificationProcessorProgressRepository;
import me.geohod.geohodbackend.service.INotificationProcessorProgressService;

@Service
@RequiredArgsConstructor
public class NotificationProcessorProgressServiceImpl implements INotificationProcessorProgressService {

    private final NotificationProcessorProgressRepository progressRepository;

    @Override
    public void updateProgress(String processorName, Instant lastProcessedCreatedAt, UUID lastProcessedId) {
        NotificationProcessorProgress progress = progressRepository.findByProcessorName(processorName)
                .orElseGet(() -> new NotificationProcessorProgress(processorName, lastProcessedCreatedAt, lastProcessedId));

        progress.updateProgress(lastProcessedCreatedAt, lastProcessedId);
        progressRepository.save(progress);
    }
}
