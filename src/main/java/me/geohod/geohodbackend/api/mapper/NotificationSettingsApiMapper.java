package me.geohod.geohodbackend.api.mapper;

import me.geohod.geohodbackend.api.dto.response.NotificationSettingResponse;
import me.geohod.geohodbackend.data.dto.NotificationSettingDto;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface NotificationSettingsApiMapper {

    @Mapping(target = "eventType", source = "type")
    NotificationSettingResponse toResponse(NotificationSettingDto dto);
}
