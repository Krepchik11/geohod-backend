package me.geohod.geohodbackend.data.dto;

import java.util.stream.Stream;

public record EventParticipantDto(
        String id,
        String username,
        String firstName,
        String lastName,
        String imageUrl
) {
    public String getName() {
        return Stream.of(firstName, lastName)
                .filter(name -> name != null && !name.isBlank())
                .reduce((s1, s2) -> s1 + " " + s2)
                .orElse("");
    }
}
