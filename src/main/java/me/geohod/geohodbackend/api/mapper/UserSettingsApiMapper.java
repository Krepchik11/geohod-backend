package me.geohod.geohodbackend.api.mapper;

import org.mapstruct.Mapper;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;
import me.geohod.geohodbackend.api.dto.request.UserSettingsRequest;
import me.geohod.geohodbackend.api.dto.response.UserSettingsResponse;
import me.geohod.geohodbackend.data.dto.UserSettingsDto;

@Mapper(config = GlobalMapperConfig.class)
public interface UserSettingsApiMapper {
    UserSettingsResponse toResponse(UserSettingsDto dto);

    UserSettingsDto fromRequest(UserSettingsRequest request);
}
