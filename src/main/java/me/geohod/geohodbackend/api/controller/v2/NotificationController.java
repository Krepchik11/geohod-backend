package me.geohod.geohodbackend.api.controller.v2;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.notification.NotificationCursorRequest;
import me.geohod.geohodbackend.api.dto.notification.NotificationResponse;
import me.geohod.geohodbackend.api.mapper.NotificationApiMapper;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.data.model.notification.Notification;
import me.geohod.geohodbackend.service.IAppNotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final IAppNotificationService notificationService;
    private final NotificationApiMapper notificationApiMapper;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getNotifications(@RequestParam UUID userId, NotificationCursorRequest cursorRequest) {
        List<Notification> notifications = notificationService.getNotifications(userId, cursorRequest);
        List<NotificationResponse> response = notifications.stream().map(notificationApiMapper::map).toList();
        return ApiResponse.success(response);
    }

    @PostMapping("/{id}/mark-as-read")
    public ApiResponse<Void> markAsRead(@PathVariable UUID id, @RequestParam UUID userId) {
        notificationService.markAsRead(id, userId);
        return ApiResponse.success(null);
    }

    @PostMapping("/mark-all-as-read")
    public ApiResponse<Void> markAllAsRead(@RequestParam UUID userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.success(null);
    }
} 