package me.geohod.geohodbackend.data.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import me.geohod.geohodbackend.data.dto.NotificationDto;
import me.geohod.geohodbackend.data.model.notification.Notification;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;
import me.geohod.geohodbackend.service.notification.NotificationType;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;

@Mapper(config = GlobalMapperConfig.class)
public interface NotificationMapper {
    @Mapping(target = "payload", expression = "java(notification.getPayload() != null ? notification.getPayload().value() : null)")
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "type", source = "notification.type")
    NotificationDto toDto(Notification notification);

    default NotificationType mapStrategyToNotificationType(StrategyNotificationType strategyType) {
        if (strategyType == null) {
            return null;
        }
        return switch (strategyType) {
            case EVENT_CREATED -> NotificationType.EVENT_CREATED;
            case EVENT_CANCELLED -> NotificationType.EVENT_CANCELLED;
            case EVENT_FINISHED -> NotificationType.EVENT_FINISHED;
            case PARTICIPANT_REGISTERED -> NotificationType.PARTICIPANT_REGISTERED;
            case PARTICIPANT_UNREGISTERED -> NotificationType.PARTICIPANT_UNREGISTERED;
        };
    }
}
