package me.geohod.geohodbackend.data.model.repository;

import me.geohod.geohodbackend.data.model.UserSettings;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserSettingsRepository extends CrudRepository<UserSettings, UUID> {
    Optional<UserSettings> findByUserId(UUID userId);
}