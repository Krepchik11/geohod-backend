package me.geohod.geohodbackend.data.mapper;

import org.mapstruct.Mapper;

import me.geohod.geohodbackend.api.dto.response.PaymentGatewayUrlResponse;
import me.geohod.geohodbackend.data.dto.PaymentGatewayInfoDto;
import me.geohod.geohodbackend.mapper.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface PaymentGatewayModelMapper {
    PaymentGatewayUrlResponse toResponse(PaymentGatewayInfoDto dto);
}