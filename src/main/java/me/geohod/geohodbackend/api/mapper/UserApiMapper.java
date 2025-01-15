package me.geohod.geohodbackend.api.mapper;

import me.geohod.geohodbackend.api.dto.response.TelegramUserDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserApiMapper {
    default TelegramUserDetails map(me.geohod.geohodbackend.data.dto.TelegramUserDetails dto) {
        return new TelegramUserDetails(dto.username(), String.join(" ", dto.firstName(), dto.lastName()), dto.imageUrl());
    }
}
