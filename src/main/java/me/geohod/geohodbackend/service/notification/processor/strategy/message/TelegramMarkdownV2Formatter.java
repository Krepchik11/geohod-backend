package me.geohod.geohodbackend.service.notification.processor.strategy.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class TelegramMarkdownV2Formatter {
    
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");
    
    public String format(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        
        return processLinks(message);
    }
    
    private String processLinks(String message) {
        StringBuilder result = new StringBuilder();
        int lastPosition = 0;
        
        Matcher matcher = LINK_PATTERN.matcher(message);
        
        while (matcher.find()) {
            // Add properly escaped text before the link
            String beforeLink = message.substring(lastPosition, matcher.start());
            result.append(escapeText(beforeLink));
            
            // Process the link with correct escaping rules
            String linkText = matcher.group(1);
            String linkUrl = matcher.group(2);
            result.append(createLink(linkText, linkUrl));
            
            lastPosition = matcher.end();
        }
        
        // Add remaining text after the last link
        result.append(escapeText(message.substring(lastPosition)));
        
        return result.toString();
    }
    
    private String createLink(String text, String url) {
        String escapedText = escapeText(text);
        String escapedUrl = escapeUrl(url);
        return "[" + escapedText + "](" + escapedUrl + ")";
    }
    
    /**
     * Escapes text according to Telegram MarkdownV2 rules.
     * Escapes: _, *, [, ], (, ), ~, `, >, #, +, -, =, |, {, }, ., !
     */
    private String escapeText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        return text.replace("\\", "\\\\")    // First to avoid double-escaping
                   .replace("_", "\\_")
                   .replace("*", "\\*")
                   .replace("[", "\\[")
                   .replace("]", "\\]")
                   .replace("(", "\\(")
                   .replace(")", "\\)")
                   .replace("~", "\\~")
                   .replace("`", "\\`")
                   .replace(">", "\\>")
                   .replace("#", "\\#")
                   .replace("+", "\\+")
                   .replace("-", "\\-")
                   .replace("=", "\\=")
                   .replace("|", "\\|")
                   .replace("{", "\\{")
                   .replace("}", "\\}")
                   .replace(".", "\\.")
                   .replace("!", "\\!");
    }
    
    /**
     * Escapes URL according to Telegram MarkdownV2 rules.
     * Inside URL part, only ) and \ must be escaped.
     */
    private String escapeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        
        return url.replace("\\", "\\\\").replace(")", "\\)");
    }
}