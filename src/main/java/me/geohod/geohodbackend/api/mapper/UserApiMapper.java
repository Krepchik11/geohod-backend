package me.geohod.geohodbackend.api.mapper;

import me.geohod.geohodbackend.api.dto.response.TelegramUserDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserApiMapper {
    TelegramUserDetails map(me.geohod.geohodbackend.data.dto.TelegramUserDetails dto);
}
