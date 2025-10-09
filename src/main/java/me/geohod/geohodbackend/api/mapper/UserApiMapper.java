package me.geohod.geohodbackend.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import me.geohod.geohodbackend.api.dto.response.EventParticipantDetails;
import me.geohod.geohodbackend.api.dto.response.UserDetailsResponse;
import me.geohod.geohodbackend.api.dto.response.UserResponse;
import me.geohod.geohodbackend.data.dto.EventParticipantProjection;
import me.geohod.geohodbackend.data.dto.UserDto;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface UserApiMapper {
    EventParticipantDetails map(EventParticipantProjection dto);

    UserResponse map(UserDto user);

    @Mapping(target = "name", expression = "java(mapFullName(user.getFirstName(), user.getLastName()))")
    @Mapping(target = "username", source = "tgUsername")
    @Mapping(target = "imageUrl", source = "tgImageUrl")
    UserDetailsResponse mapToDetails(User user);

    default String mapFullName(String firstName, String lastName) {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null) {
            fullName.append(firstName);
        }
        if (lastName != null) {
            if (!fullName.isEmpty()) {
                fullName.append(" ");
            }
            fullName.append(lastName);
        }
        return !fullName.isEmpty() ? fullName.toString() : null;
    }

    default java.util.UUID map(String id) {
        return id != null ? java.util.UUID.fromString(id) : null;
    }
}
