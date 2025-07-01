package me.geohod.geohodbackend.user_settings.service;

import me.geohod.geohodbackend.user_settings.data.model.UserSettings;
import me.geohod.geohodbackend.user_settings.data.repository.UserSettingsRepository;
import me.geohod.geohodbackend.user_settings.mapper.UserSettingsMapper;
import me.geohod.geohodbackend.user_settings.service.dto.UserSettingsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSettingsServiceTest {
    @Mock
    private UserSettingsRepository repository;
    @Mock
    private UserSettingsMapper mapper;
    @InjectMocks
    private UserSettingsServiceImpl service;

    private UUID userId;
    private UserSettings entity;
    private UserSettingsDto dto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        entity = new UserSettings("10", 5);
        entity.setId(userId);
        dto = new UserSettingsDto("10", 5);
    }

    @Test
    void getUserSettings_whenExists_returnsMappedDto() {
        when(repository.findByUserId(userId)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        UserSettingsDto result = service.getUserSettings(userId);
        assertEquals(dto, result);
    }

    @Test
    void getUserSettings_whenNotExists_returnsNullDto() {
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());
        UserSettingsDto result = service.getUserSettings(userId);
        assertNull(result.defaultDonationAmount());
        assertNull(result.defaultMaxParticipants());
    }

    @Test
    void updateUserSettings_whenExists_updatesAndReturnsDto() {
        when(repository.findByUserId(userId)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);
        UserSettingsDto input = new UserSettingsDto("20", 10);
        UserSettingsDto result = service.updateUserSettings(userId, input);
        assertEquals(dto, result);
    }

    @Test
    void updateUserSettings_whenNotExists_createsAndReturnsDto() {
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(any(UserSettings.class))).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);
        UserSettingsDto result = service.updateUserSettings(userId, dto);
        assertEquals(dto, result);
    }

    @Test
    void updateUserSettings_handlesNulls() {
        UserSettingsDto input = new UserSettingsDto(null, null);
        UserSettings entityWithNulls = new UserSettings(null, null);
        entityWithNulls.setId(userId);
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());
        when(mapper.toEntity(input)).thenReturn(entityWithNulls);
        when(repository.save(any(UserSettings.class))).thenReturn(entityWithNulls);
        when(mapper.toDto(entityWithNulls)).thenReturn(input);
        UserSettingsDto result = service.updateUserSettings(userId, input);
        assertNull(result.defaultDonationAmount());
        assertNull(result.defaultMaxParticipants());
    }
} 