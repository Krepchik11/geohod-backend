package me.geohod.geohodbackend.api.mapper;

import me.geohod.geohodbackend.api.dto.notification.NotificationResponse;
import me.geohod.geohodbackend.data.model.notification.Notification;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface NotificationApiMapper {
    NotificationResponse map(Notification notification);
} 