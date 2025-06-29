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

import java.util.List;

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
            @RequestParam(required = false) Long cursorIdAfter) {
        List<Notification> notifications = appNotificationService.getNotifications(
                principal.userId(), limit, isRead, cursorIdAfter);
        List<NotificationResponse> response = notifications.stream().map(notificationApiMapper::map).toList();
        return ApiResponse.success(response);
    }

    @PostMapping("/{id}/dismiss")
    public ApiResponse<Void> dismiss(@AuthenticationPrincipal TelegramPrincipal principal, @PathVariable Long id) {
        appNotificationService.dismiss(id, principal.userId());
        return ApiResponse.success(null);
    }

    @PostMapping("/dismiss-all")
    public ApiResponse<Void> dismissAll(@AuthenticationPrincipal TelegramPrincipal principal) {
        appNotificationService.dismissAll(principal.userId());
        return ApiResponse.success(null);
    }
} 