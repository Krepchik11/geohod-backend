package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.notification.NotificationProcessorProgress;
import me.geohod.geohodbackend.data.model.repository.NotificationProcessorProgressRepository;
import me.geohod.geohodbackend.service.INotificationProcessorProgressService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationProcessorProgressServiceImpl implements INotificationProcessorProgressService {

    private final NotificationProcessorProgressRepository progressRepository;

    @Override
    public void updateProgress(String processorName, UUID lastProcessedEventLogId) {
        NotificationProcessorProgress progress = progressRepository.findByProcessorName(processorName)
                .orElseGet(() -> new NotificationProcessorProgress(processorName, lastProcessedEventLogId));

        progress.updateProgress(lastProcessedEventLogId);
        progressRepository.save(progress);
    }
} 