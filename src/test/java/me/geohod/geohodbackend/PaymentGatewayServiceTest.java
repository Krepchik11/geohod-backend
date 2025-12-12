package me.geohod.geohodbackend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.geohod.geohodbackend.data.dto.PaymentGatewayInfoDto;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.UserSettings;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.data.model.repository.UserSettingsRepository;
import me.geohod.geohodbackend.exception.ResourceNotFoundException;
import me.geohod.geohodbackend.service.IPaymentGatewayService;
import me.geohod.geohodbackend.service.impl.PaymentGatewayServiceImpl;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    private IPaymentGatewayService paymentGatewayService;

    private UUID eventId;
    private UUID authorId;
    private Event event;
    private UserSettings authorSettings;

    @BeforeEach
    void setUp() {
        paymentGatewayService = new PaymentGatewayServiceImpl(
                eventRepository,
                userSettingsRepository
        );

        eventId = UUID.randomUUID();
        authorId = UUID.randomUUID();

        event = new Event("Test Event", "Description", Instant.now(), 10, authorId);
        authorSettings = new UserSettings(authorId, "10.00", 5, "https://payment.example.com/donate", true, "1234567890");
    }

    @Test
    void testGetEventAuthorPaymentGateway_Success() {
        // Given
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userSettingsRepository.findByUserId(authorId)).thenReturn(Optional.of(authorSettings));

        PaymentGatewayInfoDto result = paymentGatewayService.getEventAuthorPaymentGateway(eventId);

        assertEquals("https://payment.example.com/donate", result.paymentUrl());

        verify(eventRepository).findById(eventId);
        verify(userSettingsRepository).findByUserId(authorId);
    }

    @Test
    void testGetEventAuthorPaymentGateway_EventNotFound() {
        // Given
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> paymentGatewayService.getEventAuthorPaymentGateway(eventId));

        verify(eventRepository).findById(eventId);
        verify(userSettingsRepository, never()).findByUserId(any());
    }

    @Test
    void testGetEventAuthorPaymentGateway_AuthorSettingsNotFound() {
        // Given
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userSettingsRepository.findByUserId(authorId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> paymentGatewayService.getEventAuthorPaymentGateway(eventId));

        verify(eventRepository).findById(eventId);
        verify(userSettingsRepository).findByUserId(authorId);
    }

    @Test
    void testGetEventAuthorPaymentGateway_NoPaymentGatewayUrl() {
        // Given
        UserSettings settingsWithoutPaymentUrl = new UserSettings(authorId, "10.00", 5, null, true, "1234567890");
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userSettingsRepository.findByUserId(authorId)).thenReturn(Optional.of(settingsWithoutPaymentUrl));

        assertThrows(ResourceNotFoundException.class, 
                () -> paymentGatewayService.getEventAuthorPaymentGateway(eventId));

        verify(eventRepository).findById(eventId);
        verify(userSettingsRepository).findByUserId(authorId);
    }

    @Test
    void testGetEventAuthorPaymentGateway_EmptyPaymentGatewayUrl() {
        // Given
        UserSettings settingsWithEmptyPaymentUrl = new UserSettings(authorId, "10.00", 5, "", true, "1234567890");
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userSettingsRepository.findByUserId(authorId)).thenReturn(Optional.of(settingsWithEmptyPaymentUrl));

        assertThrows(ResourceNotFoundException.class, 
                () -> paymentGatewayService.getEventAuthorPaymentGateway(eventId));

        verify(eventRepository).findById(eventId);
        verify(userSettingsRepository).findByUserId(authorId);
    }
}