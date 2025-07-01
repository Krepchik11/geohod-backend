package me.geohod.geohodbackend.user_settings.api;

import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.user_settings.api.dto.UserSettingsRequest;
import me.geohod.geohodbackend.user_settings.api.dto.UserSettingsResponse;
import me.geohod.geohodbackend.user_settings.mapper.UserSettingsMapper;
import me.geohod.geohodbackend.user_settings.service.IUserSettingsService;
import me.geohod.geohodbackend.user_settings.service.dto.UserSettingsDto;
import me.geohod.geohodbackend.security.principal.TelegramPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
} 