package me.geohod.geohodbackend.api.mapper;

import me.geohod.geohodbackend.api.dto.response.EventParticipantDetails;
import me.geohod.geohodbackend.api.dto.response.UserResponse;
import me.geohod.geohodbackend.data.dto.EventParticipantProjection;
import me.geohod.geohodbackend.data.dto.UserDto;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface UserApiMapper {
    EventParticipantDetails map(EventParticipantProjection dto);
    UserResponse map(UserDto user);
}
