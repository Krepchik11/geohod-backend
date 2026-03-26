package me.geohod.geohodbackend.service;

import java.util.UUID;

import me.geohod.geohodbackend.data.dto.PaymentGatewayInfoDto;

public interface IPaymentGatewayService {
    PaymentGatewayInfoDto getEventAuthorPaymentGateway(UUID eventId);
}