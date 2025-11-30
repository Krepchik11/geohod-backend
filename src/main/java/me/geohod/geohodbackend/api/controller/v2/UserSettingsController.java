package me.geohod.geohodbackend.api.controller.v2;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.security.principal.TelegramPrincipal;
import me.geohod.geohodbackend.api.dto.request.DefaultMaxParticipantsRequest;
import me.geohod.geohodbackend.api.dto.request.PaymentGatewayUrlRequest;
import me.geohod.geohodbackend.api.dto.request.ShowBecomeOrganizerRequest;
import me.geohod.geohodbackend.api.dto.request.UserSettingsRequest;
import me.geohod.geohodbackend.api.dto.response.UserSettingsResponse;
import me.geohod.geohodbackend.api.mapper.UserSettingsApiMapper;
import me.geohod.geohodbackend.service.IUserSettingsService;
import me.geohod.geohodbackend.data.dto.UserSettingsDto;

@RestController
@RequestMapping("/api/v2/user/settings")
public class UserSettingsController {
    private final IUserSettingsService userSettingsService;
    private final UserSettingsApiMapper userSettingsApiMapper;

    public UserSettingsController(IUserSettingsService userSettingsService,
            UserSettingsApiMapper userSettingsApiMapper) {
        this.userSettingsService = userSettingsService;
        this.userSettingsApiMapper = userSettingsApiMapper;
    }

    @GetMapping
    public ApiResponse<UserSettingsResponse> getUserSettings(@AuthenticationPrincipal TelegramPrincipal principal) {
        UserSettingsDto dto = userSettingsService.getUserSettings(principal.userId());
        UserSettingsResponse response = userSettingsApiMapper.toResponse(dto);
        return ApiResponse.success(response);
    }

    @PutMapping
    public ApiResponse<UserSettingsResponse> updateUserSettings(
            @AuthenticationPrincipal TelegramPrincipal principal,
            @RequestBody UserSettingsRequest request) {
        UserSettingsDto dto = userSettingsApiMapper.fromRequest(request);
        UserSettingsDto updated = userSettingsService.updateUserSettings(principal.userId(), dto);
        UserSettingsResponse response = userSettingsApiMapper.toResponse(updated);
        return ApiResponse.success(response);
    }

    @PutMapping("/default-max-participants")
    public ApiResponse<UserSettingsResponse> updateMaxParticipants(
            @AuthenticationPrincipal TelegramPrincipal principal,
            @Valid @RequestBody DefaultMaxParticipantsRequest request) {
        UserSettingsDto dto = userSettingsService.getUserSettings(principal.userId());
        UserSettingsDto updated = new UserSettingsDto(dto.defaultDonationAmount(), request.defaultMaxParticipants(),
                dto.paymentGatewayUrl(), dto.showBecomeOrganizer());
        UserSettingsDto saved = userSettingsService.updateUserSettings(principal.userId(), updated);
        UserSettingsResponse response = userSettingsApiMapper.toResponse(saved);
        return ApiResponse.success(response);
    }

    @PutMapping("/payment-gateway-url")
    public ApiResponse<UserSettingsResponse> updatePaymentGatewayUrl(
            @AuthenticationPrincipal TelegramPrincipal principal,
            @Valid @RequestBody PaymentGatewayUrlRequest request) {
        UserSettingsDto dto = userSettingsService.getUserSettings(principal.userId());
        UserSettingsDto updated = new UserSettingsDto(dto.defaultDonationAmount(), dto.defaultMaxParticipants(),
                request.paymentGatewayUrl(), dto.showBecomeOrganizer());
        UserSettingsDto saved = userSettingsService.updateUserSettings(principal.userId(), updated);
        UserSettingsResponse response = userSettingsApiMapper.toResponse(saved);
        return ApiResponse.success(response);
    }

    @PutMapping("/show-become-organizer")
    public ApiResponse<UserSettingsResponse> updateShowBecomeOrganizer(
            @AuthenticationPrincipal TelegramPrincipal principal,
            @Valid @RequestBody ShowBecomeOrganizerRequest request) {
        UserSettingsDto dto = userSettingsService.getUserSettings(principal.userId());
        UserSettingsDto updated = new UserSettingsDto(dto.defaultDonationAmount(), dto.defaultMaxParticipants(),
                dto.paymentGatewayUrl(), request.showBecomeOrganizer());
        UserSettingsDto saved = userSettingsService.updateUserSettings(principal.userId(), updated);
        UserSettingsResponse response = userSettingsApiMapper.toResponse(saved);
        return ApiResponse.success(response);
    }

}