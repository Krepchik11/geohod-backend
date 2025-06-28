package me.geohod.geohodbackend.api.controller.v2;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.notification.NotificationResponse;
import me.geohod.geohodbackend.api.mapper.NotificationApiMapper;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.data.model.notification.Notification;
import me.geohod.geohodbackend.security.principal.TelegramPrincipal;
import me.geohod.geohodbackend.service.IAppNotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final IAppNotificationService appNotificationService;
    private final NotificationApiMapper notificationApiMapper;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal TelegramPrincipal principal,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "false") Boolean isRead,
            @RequestParam(required = false) Instant cursorCreatedAt) {
        List<Notification> notifications = appNotificationService.getNotifications(
                principal.userId(), limit, isRead, cursorCreatedAt);
        List<NotificationResponse> response = notifications.stream().map(notificationApiMapper::map).toList();
        return ApiResponse.success(response);
    }

    @PostMapping("/{id}/mark-as-read")
    public ApiResponse<Void> markAsRead(@AuthenticationPrincipal TelegramPrincipal principal, @PathVariable UUID id) {
        appNotificationService.markAsRead(id, principal.userId());
        return ApiResponse.success(null);
    }

    @PostMapping("/mark-all-as-read")
    public ApiResponse<Void> markAllAsRead(@AuthenticationPrincipal TelegramPrincipal principal) {
        appNotificationService.markAllAsRead(principal.userId());
        return ApiResponse.success(null);
    }
} 