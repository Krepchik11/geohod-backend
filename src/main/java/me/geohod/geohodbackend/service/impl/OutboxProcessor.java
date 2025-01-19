package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.data.model.TelegramOutboxMessage;
import me.geohod.geohodbackend.data.model.repository.TelegramOutboxMessageRepository;
import me.geohod.geohodbackend.service.INotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
class OutboxProcessor {
    private static final int BATCH_SIZE = 30;
    
    private final TelegramOutboxMessageRepository outboxRepository;
    private final INotificationService notificationService;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void processOutbox() {
        Page<TelegramOutboxMessage> unprocessed = outboxRepository
            .findAllByProcessedIsFalse(PageRequest.of(0, BATCH_SIZE));
            
        List<TelegramOutboxMessage> processedMessages = new ArrayList<>();
        
        for (TelegramOutboxMessage message : unprocessed) {
            try {
                notificationService.sendNotification(message.getRecipientUserId(), message.getMessage());
                message.markProcessed();
                processedMessages.add(message);
            } catch (Exception e) {
                log.error("Failed to process message {}: {}", message.getId(), e.getMessage(), e);
            }
        }
        
        if (!processedMessages.isEmpty()) {
            outboxRepository.saveAll(processedMessages);
        }
    }
}