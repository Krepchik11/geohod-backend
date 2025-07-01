package me.geohod.geohodbackend.user_settings.mapper;

import me.geohod.geohodbackend.user_settings.data.model.UserSettings;
import me.geohod.geohodbackend.user_settings.api.dto.UserSettingsResponse;
import org.mapstruct.Mapper;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface UserSettingsMapper {
    UserSettingsResponse toResponse(UserSettings userSettings);
} 