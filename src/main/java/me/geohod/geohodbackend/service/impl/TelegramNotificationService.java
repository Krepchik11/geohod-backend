package me.geohod.geohodbackend.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.data.model.repository.UserRepository;
import me.geohod.geohodbackend.service.ITelegramBotService;
import me.geohod.geohodbackend.service.ITelegramNotificationService;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements ITelegramNotificationService {
    private final ITelegramBotService telegramBotService;
    private final UserRepository userRepository;

    @Override
    public void sendNotification(UUID userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        Long chatId = Long.parseLong(user.getTgId());
        String escapedMessage = escapeMarkdownV2(message);
        telegramBotService.sendMessage(chatId, escapedMessage);
    }

    public String escapeMarkdownV2(String text) {
        if (text == null) {
            return null;
        }
        
        // Characters that need to be escaped in Telegram MarkdownV2
        String[] specialChars = {"_", "*", "[", "]", "(", ")", "~", "`", ">", "#", "+", "-", "=", "|", "{", "}", ".", "!"};
        
        String escaped = text;
        for (String ch : specialChars) {
            escaped = escaped.replace(ch, "\\" + ch);
        }
        
        return escaped;
    }
}
