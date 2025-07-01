package me.geohod.geohodbackend.user_settings.data.repository;

import me.geohod.geohodbackend.user_settings.data.model.UserSettings;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserSettingsRepository extends CrudRepository<UserSettings, UUID> {
    Optional<UserSettings> findByUserId(UUID userId);
} 