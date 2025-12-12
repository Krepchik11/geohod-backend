package me.geohod.geohodbackend.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import me.geohod.geohodbackend.data.dto.PaymentGatewayInfoDto;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.UserSettings;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.data.model.repository.UserSettingsRepository;
import me.geohod.geohodbackend.exception.ResourceNotFoundException;
import me.geohod.geohodbackend.service.IPaymentGatewayService;

@Service
@Transactional(readOnly = true)
public class PaymentGatewayServiceImpl implements IPaymentGatewayService {

    private final EventRepository eventRepository;
    private final UserSettingsRepository userSettingsRepository;

    public PaymentGatewayServiceImpl(
            EventRepository eventRepository,
            UserSettingsRepository userSettingsRepository) {
        this.eventRepository = eventRepository;
        this.userSettingsRepository = userSettingsRepository;
    }

    @Override
    public PaymentGatewayInfoDto getEventAuthorPaymentGateway(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        UserSettings authorSettings = userSettingsRepository.findByUserId(event.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Event author settings not found: " + event.getAuthorId()));

        if (authorSettings.getPaymentGatewayUrl() == null || authorSettings.getPaymentGatewayUrl().trim().isEmpty()) {
            throw new ResourceNotFoundException("Event author has not configured a payment gateway URL");
        }

        return new PaymentGatewayInfoDto(
                authorSettings.getPaymentGatewayUrl()
        );
    }
}