package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.service.INotificationService;
import me.geohod.geohodbackend.service.ITelegramBotService;
import me.geohod.geohodbackend.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements INotificationService {
    private final ITelegramBotService botService;
    private final IUserService userService;

    @Override
    public void sendNotification(UUID userId, String message) {
        Long tgId = getUserTgId(userId);
        botService.sendMessage(tgId, message);
    }

    private Long getUserTgId(UUID userId) {
        return Long.valueOf(userService.getUser(userId).getTgId());
    }
}