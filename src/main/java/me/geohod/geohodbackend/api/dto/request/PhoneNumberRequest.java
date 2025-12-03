package me.geohod.geohodbackend.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PhoneNumberRequest(
        @NotBlank String phoneNumber) {
}
