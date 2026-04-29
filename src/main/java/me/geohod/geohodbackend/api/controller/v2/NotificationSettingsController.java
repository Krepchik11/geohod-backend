package me.geohod.geohodbackend.api.controller.v2;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.request.NotificationChannelUpdateRequest;
import me.geohod.geohodbackend.api.dto.response.NotificationSettingResponse;
import me.geohod.geohodbackend.api.mapper.NotificationSettingsApiMapper;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.data.dto.NotificationSettingDto;
import me.geohod.geohodbackend.security.principal.AppPrincipal;
import me.geohod.geohodbackend.service.IUserNotificationSettingsService;
import me.geohod.geohodbackend.service.notification.NotificationChannel;
import me.geohod.geohodbackend.service.notification.NotificationRole;
import me.geohod.geohodbackend.service.notification.processor.strategy.StrategyNotificationType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/user/settings/notifications")
@RequiredArgsConstructor
public class NotificationSettingsController {

    private final IUserNotificationSettingsService notificationSettingsService;
    private final NotificationSettingsApiMapper mapper;

    @GetMapping
    public ApiResponse<List<NotificationSettingResponse>> getSettings(
            @AuthenticationPrincipal AppPrincipal principal) {
        List<NotificationSettingDto> settings = notificationSettingsService.getSettings(principal.userId());
        return ApiResponse.success(settings.stream().map(mapper::toResponse).toList());
    }

    @PutMapping("/{notificationType}/{role}/{channel}")
    public ApiResponse<NotificationSettingResponse> updateChannelSetting(
            @AuthenticationPrincipal AppPrincipal principal,
            @PathVariable StrategyNotificationType notificationType,
            @PathVariable NotificationRole role,
            @PathVariable NotificationChannel channel,
            @RequestBody NotificationChannelUpdateRequest request) {
        NotificationSettingDto updated = notificationSettingsService.updateChannelSetting(
                principal.userId(), notificationType, role, channel, request.enabled());
        return ApiResponse.success(mapper.toResponse(updated));
    }
}
