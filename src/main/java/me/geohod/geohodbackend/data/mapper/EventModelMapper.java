package me.geohod.geohodbackend.data.mapper;

import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface EventModelMapper {
    EventDto map(Event event);
}
