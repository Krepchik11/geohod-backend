package me.geohod.geohodbackend.api.mapper;

import me.geohod.geohodbackend.api.dto.request.EventCreateRequest;
import me.geohod.geohodbackend.api.dto.request.EventUpdateRequest;
import me.geohod.geohodbackend.api.dto.response.EventDetailsResponse;
import me.geohod.geohodbackend.data.dto.CreateEventDto;
import me.geohod.geohodbackend.data.dto.EventDetailedProjection;
import me.geohod.geohodbackend.data.dto.UpdateEventDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventApiMapper {
    CreateEventDto map(EventCreateRequest request, UUID authorId);

    UpdateEventDto map(EventUpdateRequest request, UUID eventId);

    EventDetailsResponse response(EventDetailedProjection projection);
}
