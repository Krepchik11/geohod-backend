package me.geohod.geohodbackend.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import me.geohod.geohodbackend.api.dto.response.EventParticipantDetails;
import me.geohod.geohodbackend.data.dto.EventParticipantDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserApiMapper {
    EventParticipantDetails map(EventParticipantDto dto);
}
