package me.geohod.geohodbackend.data.model.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import me.geohod.geohodbackend.data.model.TelegramOutboxMessage;

public interface TelegramOutboxMessageRepository extends CrudRepository<TelegramOutboxMessage, UUID> {
    Page<TelegramOutboxMessage> findAllByProcessedIsFalse(Pageable pageable);
}