package me.geohod.geohodbackend.user_settings.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import me.geohod.geohodbackend.mapper.GlobalMapperConfig;
import me.geohod.geohodbackend.user_settings.api.dto.UserSettingsRequest;
import me.geohod.geohodbackend.user_settings.api.dto.UserSettingsResponse;
import me.geohod.geohodbackend.user_settings.data.model.UserSettings;
import me.geohod.geohodbackend.user_settings.service.dto.UserSettingsDto;

@Mapper(config = GlobalMapperConfig.class)
public interface UserSettingsMapper {
    @Mapping(target = "paymentGatewayUrl", constant = "")
    @Mapping(target = "showBecomeOrganizer", constant = "true")
    UserSettingsResponse toResponse(UserSettingsDto dto);
    UserSettingsDto toDto(UserSettings userSettings);
    UserSettings toEntity(UserSettingsDto dto);
    UserSettingsDto fromRequest(UserSettingsRequest request);
} 