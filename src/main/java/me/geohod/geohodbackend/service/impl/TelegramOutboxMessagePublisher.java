package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.TelegramOutboxMessage;
import me.geohod.geohodbackend.data.model.repository.TelegramOutboxMessageRepository;
import me.geohod.geohodbackend.service.ITelegramOutboxMessagePublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TelegramOutboxMessagePublisher implements ITelegramOutboxMessagePublisher {
    private final TelegramOutboxMessageRepository repository;

    @Override
    public void publish(UUID userId, String message) {
        TelegramOutboxMessage outboxMessage = new TelegramOutboxMessage(userId, message);
        repository.save(outboxMessage);
    }
}
