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
        
        // First escape backslashes (must be done first to avoid double-escaping)
        String escaped = text.replace("\\", "\\\\");
        
        // Handle markdown links specially - preserve [text](url) structure
        // Regex to match meaningful markdown links: [non-empty text](non-empty url)
        String linkPattern = "\\[([^\\]]+)\\]\\(([^)]+)\\)";
        
        // Use StringBuilder for efficient string manipulation
        StringBuilder result = new StringBuilder();
        java.util.regex.Matcher linkMatcher = java.util.regex.Pattern.compile(linkPattern).matcher(escaped);
        
        int lastEnd = 0;
        while (linkMatcher.find()) {
            // Escape text before the link
            String beforeLink = escaped.substring(lastEnd, linkMatcher.start());
            result.append(escapeTextOutsideLinks(beforeLink));
            
            // Process the link: [linkText](url)
            String linkText = linkMatcher.group(1);
            String url = linkMatcher.group(2);
            
            // Escape link text (but not the brackets)
            String escapedLinkText = escapeTextOutsideLinks(linkText);
            
            // In URL part, only escape ) and \ (\ already escaped above)
            // Also escape dots and dashes in URLs for Telegram
            String escapedUrl = url.replace(")", "\\)").replace(".", "\\.").replace("-", "\\-");
            
            // Reconstruct the link - preserve [ ] ( ) structure
            result.append("[").append(escapedLinkText).append("](").append(escapedUrl).append(")");
            
            lastEnd = linkMatcher.end();
        }
        
        // Escape remaining text after the last link
        String afterLinks = escaped.substring(lastEnd);
        result.append(escapeTextOutsideLinks(afterLinks));
        
        return result.toString();
    }
    
    private String escapeTextOutsideLinks(String text) {
        // Characters that need to be escaped outside of links (\ already escaped)
        String[] specialChars = {"_", "*", "[", "]", "(", ")", "~", "`", ">", "#", "+", "-", "=", "|", "{", "}", ".", "!"};
        
        String escaped = text;
        for (String ch : specialChars) {
            escaped = escaped.replace(ch, "\\" + ch);
        }
        
        return escaped;
    }
}
