package me.geohod.geohodbackend.user_settings.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.security.principal.TelegramPrincipal;
import me.geohod.geohodbackend.user_settings.api.dto.DefaultMaxParticipantsRequest;
import me.geohod.geohodbackend.user_settings.api.dto.PaymentGatewayUrlRequest;
import me.geohod.geohodbackend.user_settings.api.dto.ShowBecomeOrganizerRequest;
import me.geohod.geohodbackend.user_settings.api.dto.UserSettingsRequest;
import me.geohod.geohodbackend.user_settings.api.dto.UserSettingsResponse;
import me.geohod.geohodbackend.user_settings.mapper.UserSettingsMapper;
import me.geohod.geohodbackend.user_settings.service.IUserSettingsService;
import me.geohod.geohodbackend.user_settings.service.dto.UserSettingsDto;

@RestController
@RequestMapping("/api/v2/user/settings")
public class UserSettingsController {
    private final IUserSettingsService userSettingsService;
    private final UserSettingsMapper userSettingsMapper;

    public UserSettingsController(IUserSettingsService userSettingsService, UserSettingsMapper userSettingsMapper) {
        this.userSettingsService = userSettingsService;
        this.userSettingsMapper = userSettingsMapper;
    }

    @GetMapping
    public ApiResponse<UserSettingsResponse> getUserSettings(@AuthenticationPrincipal TelegramPrincipal principal) {
        UserSettingsDto dto = userSettingsService.getUserSettings(principal.userId());
        UserSettingsResponse response = userSettingsMapper.toResponse(dto);
        return ApiResponse.success(response);
    }

    @PutMapping
    public ApiResponse<UserSettingsResponse> updateUserSettings(
            @AuthenticationPrincipal TelegramPrincipal principal,
            @RequestBody UserSettingsRequest request) {
                UserSettingsDto dto = userSettingsMapper.fromRequest(request);
                UserSettingsDto updated = userSettingsService.updateUserSettings(principal.userId(), dto);
                UserSettingsResponse response = userSettingsMapper.toResponse(updated);
                return ApiResponse.success(response);
    }

    @PutMapping("/default-max-participants")
    public ApiResponse<UserSettingsResponse> updateMaxParticipants(
            @AuthenticationPrincipal TelegramPrincipal principal,
            @Valid @RequestBody DefaultMaxParticipantsRequest request) {
        UserSettingsDto dto = userSettingsService.getUserSettings(principal.userId());
        UserSettingsDto updated = new UserSettingsDto(dto.defaultDonationAmount(), request.defaultMaxParticipants(), dto.paymentGatewayUrl(), dto.showBecomeOrganizer());
        UserSettingsDto saved = userSettingsService.updateUserSettings(principal.userId(), updated);
        UserSettingsResponse response = userSettingsMapper.toResponse(saved);
        return ApiResponse.success(response);
    }

    @PutMapping("/payment-gateway-url")
    public ApiResponse<UserSettingsResponse> updatePaymentGatewayUrl(
            @AuthenticationPrincipal TelegramPrincipal principal,
            @Valid @RequestBody PaymentGatewayUrlRequest request) {
        UserSettingsDto dto = userSettingsService.getUserSettings(principal.userId());
        UserSettingsDto updated = new UserSettingsDto(dto.defaultDonationAmount(), dto.defaultMaxParticipants(), request.paymentGatewayUrl(), dto.showBecomeOrganizer());
        UserSettingsDto saved = userSettingsService.updateUserSettings(principal.userId(), updated);
        UserSettingsResponse response = userSettingsMapper.toResponse(saved);
        return ApiResponse.success(response);
    }

    @PutMapping("/show-become-organizer")
    public ApiResponse<UserSettingsResponse> updateShowBecomeOrganizer(
            @AuthenticationPrincipal TelegramPrincipal principal,
            @Valid @RequestBody ShowBecomeOrganizerRequest request) {
        UserSettingsDto dto = userSettingsService.getUserSettings(principal.userId());
        UserSettingsDto updated = new UserSettingsDto(dto.defaultDonationAmount(), dto.defaultMaxParticipants(), dto.paymentGatewayUrl(), request.showBecomeOrganizer());
        UserSettingsDto saved = userSettingsService.updateUserSettings(principal.userId(), updated);
        UserSettingsResponse response = userSettingsMapper.toResponse(saved);
        return ApiResponse.success(response);
    }

}