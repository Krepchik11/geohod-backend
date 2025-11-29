package me.geohod.geohodbackend.data.mapper;

import org.mapstruct.Mapper;

import me.geohod.geohodbackend.data.dto.CreateEventDto;
import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface EventModelMapper {
    EventDto map(Event event);

    Event map(CreateEventDto createDto);
}
