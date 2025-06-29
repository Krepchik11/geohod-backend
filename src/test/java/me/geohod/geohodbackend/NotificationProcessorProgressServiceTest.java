package me.geohod.geohodbackend;

import me.geohod.geohodbackend.data.model.notification.NotificationProcessorProgress;
import me.geohod.geohodbackend.data.model.repository.NotificationProcessorProgressRepository;
import me.geohod.geohodbackend.service.impl.NotificationProcessorProgressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        UUID lastProcessedId = UUID.randomUUID();
        NotificationProcessorProgress progress = new NotificationProcessorProgress(processorName, lastProcessedId);
        when(repository.findByProcessorName(processorName)).thenReturn(java.util.Optional.of(progress));
        when(repository.save(any(NotificationProcessorProgress.class))).thenReturn(progress);
        service.updateProgress(processorName, lastProcessedId);
        verify(repository, times(1)).findByProcessorName(processorName);
        verify(repository, times(1)).save(progress);
    }
} 