package me.geohod.geohodbackend.data.mapper;

import me.geohod.geohodbackend.data.dto.EventParticipantDto;
import me.geohod.geohodbackend.data.model.EventParticipant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface EventParticipantModelMapper {
    EventParticipantDto map(EventParticipant participant);
}
