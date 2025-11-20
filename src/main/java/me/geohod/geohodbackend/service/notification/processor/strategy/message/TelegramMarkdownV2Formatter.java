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
            // Add text before the link with smart escaping
            String beforeLink = message.substring(lastPosition, matcher.start());
            result.append(smartEscape(beforeLink));
            
            // Process the link with correct escaping rules
            String linkText = matcher.group(1);
            String linkUrl = matcher.group(2);
            result.append(createLink(linkText, linkUrl));
            
            lastPosition = matcher.end();
        }
        
        // Add remaining text after the last link with smart escaping
        result.append(smartEscape(message.substring(lastPosition)));
        
        return result.toString();
    }
    
    private String smartEscape(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        return text.replace("\\", "\\\\")    // First to avoid double-escaping
                   .replace("_", "\\_")
                   .replace("*", "\\*")
                   .replace("~", "\\~")
                   .replace("`", "\\`")
                   .replace(">", "\\>")
                   .replace("#", "\\#")
                   .replace("+", "\\+")
                   .replace("=", "\\=")
                   .replace("|", "\\|")
                   .replace("{", "\\{")
                   .replace("}", "\\}")
                   .replace("!", "\\!");
        
        // Note: We're NOT escaping [, ], (, ), -, . here to avoid breaking URLs and dates
    }
    
    private String createLink(String text, String url) {
        String escapedText = escapeText(text);
        String escapedUrl = escapeUrl(url);
        return "[" + escapedText + "](" + escapedUrl + ")";
    }
    
    /**
     * Per spec: escape _, *, [, ], (, ), ~, `, >, #, +, -, =, |, {, }, ., ! outside of links
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