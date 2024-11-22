package me.geohod.geohodbackend.api.dto.request;

import lombok.Data;

import java.time.Instant;

@Data
public class EventCreateRequest {
    private String name;
    private Instant date;
    private String description;
    private Integer maxParticipants;
}




