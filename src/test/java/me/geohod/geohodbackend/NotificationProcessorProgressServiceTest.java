package me.geohod.geohodbackend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import me.geohod.geohodbackend.data.model.notification.NotificationProcessorProgress;
import me.geohod.geohodbackend.data.model.repository.NotificationProcessorProgressRepository;
import me.geohod.geohodbackend.service.impl.NotificationProcessorProgressServiceImpl;

public class NotificationProcessorProgressServiceTest {
    @Mock
    private NotificationProcessorProgressRepository repository;
    private NotificationProcessorProgressServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new NotificationProcessorProgressServiceImpl(repository);
    }

    @Test
    void testUpdateProgress() {
        String processorName = "proc";
        Instant lastProcessedCreatedAt = Instant.now();
        UUID lastProcessedId = UUID.randomUUID();
        NotificationProcessorProgress progress = new NotificationProcessorProgress(processorName, lastProcessedCreatedAt, lastProcessedId);
        when(repository.findByProcessorName(processorName)).thenReturn(java.util.Optional.of(progress));
        when(repository.save(any(NotificationProcessorProgress.class))).thenReturn(progress);
        service.updateProgress(processorName, lastProcessedCreatedAt, lastProcessedId);
        verify(repository, times(1)).findByProcessorName(processorName);
        verify(repository, times(1)).save(progress);
    }
}
