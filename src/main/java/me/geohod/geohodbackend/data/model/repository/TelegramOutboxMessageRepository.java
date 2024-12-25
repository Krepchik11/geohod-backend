package me.geohod.geohodbackend.data.model.repository;

import me.geohod.geohodbackend.data.model.TelegramOutboxMessage;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

public interface TelegramOutboxMessageRepository extends CrudRepository<TelegramOutboxMessage, UUID> {
    Collection<TelegramOutboxMessage> findAllByProcessedIsFalse();
}