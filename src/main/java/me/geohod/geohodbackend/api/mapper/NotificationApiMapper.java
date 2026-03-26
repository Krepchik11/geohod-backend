package me.geohod.geohodbackend.api.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import me.geohod.geohodbackend.api.dto.notification.NotificationResponse;
import me.geohod.geohodbackend.data.dto.NotificationDto;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface NotificationApiMapper {
    
    @Mapping(target = "isRead", source = "notificationDto.read")
    NotificationResponse toResponse(NotificationDto notificationDto, UUID userId);
} 