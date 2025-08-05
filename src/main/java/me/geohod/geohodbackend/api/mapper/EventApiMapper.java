package me.geohod.geohodbackend.api.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;

import me.geohod.geohodbackend.api.dto.request.EventCreateRequest;
import me.geohod.geohodbackend.api.dto.request.EventFinishRequest;
import me.geohod.geohodbackend.api.dto.request.EventUpdateRequest;
import me.geohod.geohodbackend.api.dto.response.EventDetailsResponse;
import me.geohod.geohodbackend.data.dto.CreateEventDto;
import me.geohod.geohodbackend.data.dto.EventDetailedProjection;
import me.geohod.geohodbackend.data.dto.FinishEventDto;
import me.geohod.geohodbackend.data.dto.UpdateEventDto;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface EventApiMapper {
    CreateEventDto map(EventCreateRequest request, UUID authorId);

    UpdateEventDto map(EventUpdateRequest request, UUID eventId);

    FinishEventDto map(EventFinishRequest request, UUID eventId);

    EventDetailsResponse response(EventDetailedProjection projection);
}
