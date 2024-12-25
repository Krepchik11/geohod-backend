package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.TelegramOutboxMessage;
import me.geohod.geohodbackend.data.model.repository.TelegramOutboxMessageRepository;
import me.geohod.geohodbackend.service.INotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
class OutboxProcessor {
    private final TelegramOutboxMessageRepository outboxRepository;
    private final INotificationService notificationService;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void processOutbox() {
        Collection<TelegramOutboxMessage> unprocessed = outboxRepository.findAllByProcessedIsFalse();
        for (TelegramOutboxMessage message : unprocessed) {

            notificationService.sendNotification(message.getRecipientUserId(), message.getMessage());

            message.markProcessed();
            outboxRepository.save(message);
        }
    }
}