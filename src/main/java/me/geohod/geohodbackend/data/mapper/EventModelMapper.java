package me.geohod.geohodbackend.data.mapper;

import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface EventModelMapper {
    EventDto map(Event event);
}
