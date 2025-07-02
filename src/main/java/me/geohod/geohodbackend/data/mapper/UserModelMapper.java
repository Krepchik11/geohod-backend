package me.geohod.geohodbackend.data.mapper;

import org.mapstruct.Mapper;

import me.geohod.geohodbackend.data.dto.UserDto;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface UserModelMapper {
    UserDto toDto(User user);
}
