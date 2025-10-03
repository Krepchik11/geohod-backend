package me.geohod.geohodbackend.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record EventRegisterRequest(
        @Min(value = 1, message = "Минимум 1 участник")
        @Max(value = 3, message = "Максимум 3 участника")
        int amountOfParticipants
) {}