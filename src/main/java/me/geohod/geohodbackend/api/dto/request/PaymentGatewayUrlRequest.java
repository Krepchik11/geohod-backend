package me.geohod.geohodbackend.api.dto.request;

import jakarta.validation.constraints.Size;

public record PaymentGatewayUrlRequest(
        @Size(max = 255) String paymentGatewayUrl) {
}