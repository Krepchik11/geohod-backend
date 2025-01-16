package me.geohod.geohodbackend.api.mapper;

import me.geohod.geohodbackend.api.dto.response.EventParticipantDetails;
import me.geohod.geohodbackend.api.dto.response.TelegramUserDetails;
import me.geohod.geohodbackend.data.dto.EventParticipantDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserApiMapper {
    EventParticipantDetails map(EventParticipantDto dto);
}
