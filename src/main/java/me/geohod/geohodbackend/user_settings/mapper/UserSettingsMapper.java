package me.geohod.geohodbackend.user_settings.mapper;

import me.geohod.geohodbackend.user_settings.data.model.UserSettings;
import me.geohod.geohodbackend.user_settings.api.dto.UserSettingsRequest;
import me.geohod.geohodbackend.user_settings.api.dto.UserSettingsResponse;
import me.geohod.geohodbackend.user_settings.service.dto.UserSettingsDto;
import org.mapstruct.Mapper;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface UserSettingsMapper {
    UserSettingsResponse toResponse(UserSettingsDto dto);
    UserSettingsDto toDto(UserSettings userSettings);
    UserSettings toEntity(UserSettingsDto dto);
    UserSettingsDto fromRequest(UserSettingsRequest request);
} 