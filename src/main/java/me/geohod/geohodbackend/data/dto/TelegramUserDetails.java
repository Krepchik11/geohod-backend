package me.geohod.geohodbackend.data.dto;

import java.util.stream.Stream;

public record TelegramUserDetails(
        String id,
        String username,
        String firstName,
        String lastName,
        String imageUrl) {
    @com.fasterxml.jackson.annotation.JsonProperty("name")
    public String getName() {
        return Stream.of(firstName, lastName)
                .filter(name -> name != null && !name.isBlank())
                .reduce((s1, s2) -> s1 + " " + s2)
                .orElse("");
    }
}