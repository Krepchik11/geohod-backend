package me.geohod.geohodbackend.user_settings.api.dto;

import jakarta.validation.constraints.Size;

public record PaymentGatewayUrlRequest(
    @Size(max = 255) String paymentGatewayUrl
) {}