package me.geohod.geohodbackend.api.mapper;

import me.geohod.geohodbackend.api.dto.notification.NotificationResponse;
import me.geohod.geohodbackend.data.dto.NotificationDto;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(config = GlobalMapperConfig.class)
public interface NotificationApiMapper {
    
    @Mapping(target = "userId", source = "userId")
    NotificationResponse toResponse(NotificationDto notificationDto, UUID userId);
    
    default NotificationResponse toResponse(NotificationDto notificationDto) {
        // This method should not be used directly - userId should be provided
        throw new UnsupportedOperationException("userId must be provided when mapping to NotificationResponse");
    }
} 