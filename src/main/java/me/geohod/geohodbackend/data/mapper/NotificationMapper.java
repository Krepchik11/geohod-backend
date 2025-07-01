package me.geohod.geohodbackend.data.mapper;

import me.geohod.geohodbackend.data.dto.NotificationDto;
import me.geohod.geohodbackend.data.model.notification.Notification;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface NotificationMapper {
    @Mapping(target = "payload", expression = "java(notification.getPayload() != null ? notification.getPayload().value() : null)")
    NotificationDto toDto(Notification notification);
} 