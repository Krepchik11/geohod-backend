package me.geohod.geohodbackend.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payment gateway for event donations")
public record PaymentGatewayUrlResponse(
        @Schema(description = "URL")
        String paymentGatewayUrl
) {
}