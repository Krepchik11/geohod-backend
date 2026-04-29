package me.geohod.geohodbackend.data.model.repository;

import me.geohod.geohodbackend.data.model.UserNotificationSetting;
import me.geohod.geohodbackend.service.notification.NotificationRole;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserNotificationSettingRepository extends CrudRepository<UserNotificationSetting, UUID> {

    List<UserNotificationSetting> findAllByUserId(UUID userId);

    Optional<UserNotificationSetting> findByUserIdAndTypeAndRole(UUID userId, StrategyNotificationType type,
                                                                  NotificationRole role);
}
