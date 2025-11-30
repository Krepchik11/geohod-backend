package me.geohod.geohodbackend.data.mapper;

import org.mapstruct.Mapper;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;
import me.geohod.geohodbackend.data.model.UserSettings;
import me.geohod.geohodbackend.data.dto.UserSettingsDto;

@Mapper(config = GlobalMapperConfig.class)
public interface UserSettingsModelMapper {
    UserSettingsDto toDto(UserSettings userSettings);

    UserSettings toEntity(UserSettingsDto dto);
}
