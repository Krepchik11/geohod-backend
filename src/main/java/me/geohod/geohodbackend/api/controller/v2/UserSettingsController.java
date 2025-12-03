package me.geohod.geohodbackend.api.controller.v2;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.request.DefaultMaxParticipantsRequest;
import me.geohod.geohodbackend.api.dto.request.PaymentGatewayUrlRequest;
import me.geohod.geohodbackend.api.dto.request.PhoneNumberRequest;
import me.geohod.geohodbackend.api.dto.request.ShowBecomeOrganizerRequest;
import me.geohod.geohodbackend.api.dto.request.UserSettingsRequest;
import me.geohod.geohodbackend.api.dto.response.UserSettingsResponse;
import me.geohod.geohodbackend.api.mapper.UserSettingsApiMapper;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.data.dto.UserSettingsDto;
import me.geohod.geohodbackend.security.principal.TelegramPrincipal;
import me.geohod.geohodbackend.service.IUserSettingsService;

@RestController
@RequestMapping("/api/v2/user/settings")
@RequiredArgsConstructor
public class UserSettingsController {
    private final IUserSettingsService userSettingsService;
    private final UserSettingsApiMapper userSettingsApiMapper;

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
        var userId = principal.userId();

        UserSettingsDto dto = userSettingsApiMapper.fromRequest(request);
        UserSettingsDto updated = userSettingsService.updateUserSettings(userId, dto);
        return ApiResponse.success(userSettingsApiMapper.toResponse(updated));
    }

    @PutMapping("/default-max-participants")
    public ApiResponse<UserSettingsResponse> updateMaxParticipants(
            @AuthenticationPrincipal TelegramPrincipal principal,
            @Valid @RequestBody DefaultMaxParticipantsRequest request) {
        var userId = principal.userId();
        var maxParticipants = request.defaultMaxParticipants();

        UserSettingsDto saved = userSettingsService.updateDefaultMaxParticipants(userId, maxParticipants);

        return ApiResponse.success(userSettingsApiMapper.toResponse(saved));
    }

    @PutMapping("/payment-gateway-url")
    public ApiResponse<UserSettingsResponse> updatePaymentGatewayUrl(
            @AuthenticationPrincipal TelegramPrincipal principal,
            @Valid @RequestBody PaymentGatewayUrlRequest request) {
        var userId = principal.userId();
        var paymentGatewayUrl = request.paymentGatewayUrl();

        UserSettingsDto saved = userSettingsService.updatePaymentGatewayUrl(userId, paymentGatewayUrl);
        
        return ApiResponse.success(userSettingsApiMapper.toResponse(saved));
    }

    @PutMapping("/show-become-organizer")
    public ApiResponse<UserSettingsResponse> updateShowBecomeOrganizer(
            @AuthenticationPrincipal TelegramPrincipal principal,
            @Valid @RequestBody ShowBecomeOrganizerRequest request) {
        var userId = principal.userId();
        var showBecomeOrginizer = request.showBecomeOrganizer();

        UserSettingsDto saved = userSettingsService.updateShowBecomeOrganizer(userId, showBecomeOrginizer);

        return ApiResponse.success(userSettingsApiMapper.toResponse(saved));
    }

    @PutMapping("/phone-number")
    public ApiResponse<UserSettingsResponse> updatePhoneNumber(
            @AuthenticationPrincipal TelegramPrincipal principal,
            @Valid @RequestBody PhoneNumberRequest request) {
        var userId = principal.userId();
        var phoneNumber = request.phoneNumber();

        UserSettingsDto saved = userSettingsService.updatePhoneNumber(userId, phoneNumber);

        return ApiResponse.success(userSettingsApiMapper.toResponse(saved));
    }

}