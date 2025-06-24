package me.geohod.geohodbackend.data.model.repository;

import me.geohod.geohodbackend.data.model.notification.NotificationProcessorProgress;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationProcessorProgressRepository extends CrudRepository<NotificationProcessorProgress, UUID> {
    Optional<NotificationProcessorProgress> findByProcessorName(String processorName);
} 