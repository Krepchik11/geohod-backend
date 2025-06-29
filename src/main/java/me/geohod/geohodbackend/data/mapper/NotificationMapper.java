package me.geohod.geohodbackend.data.mapper;

import me.geohod.geohodbackend.data.dto.NotificationDto;
import me.geohod.geohodbackend.data.model.notification.Notification;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface NotificationMapper {
    NotificationDto toDto(Notification notification);
} 