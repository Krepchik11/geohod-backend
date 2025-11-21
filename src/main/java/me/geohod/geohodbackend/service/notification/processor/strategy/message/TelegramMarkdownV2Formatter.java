package me.geohod.geohodbackend.service.notification.processor.strategy.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class TelegramMarkdownV2Formatter {

    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");
    
    // Regex to identify plain URLs (for preservation - not escaping)
    private static final Pattern PLAIN_URL_PATTERN = Pattern.compile("https?://[^\\s]+");

    public String format(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        Matcher matcher = LINK_PATTERN.matcher(message);
        int lastIndex = 0;

        while (matcher.find()) {
            // 1. APPEND PRECEDING TEXT (Strictly Escaped)
            String plainText = message.substring(lastIndex, matcher.start());
            result.append(escapeMarkdownV2(plainText));

            // 2. PROCESS THE LINK
            String linkText = matcher.group(1);
            String linkUrl = matcher.group(2);
            
            // Reconstruct the link: [Escaped Text](Escaped Url)
            result.append("[")
                  .append(escapeMarkdownV2(linkText)) // Text inside [] must be escaped
                  .append("](")
                  .append(escapeUrl(linkUrl))         // URL inside () has specific rules
                  .append(")");

            lastIndex = matcher.end();
        }

        // 3. APPEND REMAINING TEXT (Strictly Escaped)
        if (lastIndex < message.length()) {
            String remaining = message.substring(lastIndex);
            result.append(escapeMarkdownV2(remaining));
        }

        return result.toString();
    }

    /**
     * STRICTLY escapes all characters reserved in MarkdownV2.
     * Preserves plain URLs to keep them readable.
     */
    private String escapeMarkdownV2(String text) {
        if (text == null) return "";
        StringBuilder result = new StringBuilder();
        Matcher urlMatcher = PLAIN_URL_PATTERN.matcher(text);
        int lastIndex = 0;
        
        while (urlMatcher.find()) {
            // Add text before URL with strict escaping
            String beforeUrl = text.substring(lastIndex, urlMatcher.start());
            result.append(escapeWithSpecialRules(beforeUrl));
            
            // Preserve the URL as-is
            result.append(urlMatcher.group());
            
            lastIndex = urlMatcher.end();
        }
        
        // Add remaining text with strict escaping
        if (lastIndex < text.length()) {
            result.append(escapeWithSpecialRules(text.substring(lastIndex)));
        }
        
        return result.toString();
    }

    /**
     * Strict escaping for text, with special rules for readability
     */
    private String escapeWithSpecialRules(String text) {
        if (text == null || text.isEmpty()) return "";
        
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (isSpecialChar(c)) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private boolean isSpecialChar(char c) {
        return switch (c) {
            case '_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '=', '|', '{', '}', '!' -> true;
            default -> false;
        };
    }

    /**
     * Escapes URL specifically for the (...) part of a link.
     * Spec: Inside (...) part... all ')' and '\' must be escaped.
     */
    private String escapeUrl(String url) {
        if (url == null) return "";
        return url.replace("\\", "\\\\")
                  .replace(")", "\\)");
    }
}