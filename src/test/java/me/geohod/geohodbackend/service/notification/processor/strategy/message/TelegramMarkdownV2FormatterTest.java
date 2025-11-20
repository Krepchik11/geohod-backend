package me.geohod.geohodbackend.service.notification.processor.strategy.message;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TelegramMarkdownV2FormatterTest {

    @Test
    void testSimpleTextEscaping() {
        TelegramMarkdownV2Formatter formatter = new TelegramMarkdownV2Formatter();
        
        String input = "Hello *world* and [test] with (parentheses)";
        String result = formatter.format(input);
        
        // Only markdown syntax characters should be escaped, brackets and parentheses without link syntax remain unescaped
        String expected = "Hello \\*world\\* and [test] with (parentheses)";
        assertEquals(expected, result);
    }
    
    @Test
    void testLinkFormatting() {
        TelegramMarkdownV2Formatter formatter = new TelegramMarkdownV2Formatter();
        
        String input = "Click [here](https://example.com) for more info";
        String result = formatter.format(input);
        
        String expected = "Click [here](https://example.com) for more info";
        assertEquals(expected, result);
    }
    
    @Test
    void testLinkWithSpecialCharacters() {
        TelegramMarkdownV2Formatter formatter = new TelegramMarkdownV2Formatter();
        
        String input = "[*Important*](https://test.com/path?param=value) text";
        String result = formatter.format(input);
        
        // The * in link text should be escaped, but brackets and parentheses in links should not be
        String expected = "[\\*Important\\*](https://test.com/path?param=value) text";
        assertEquals(expected, result);
    }
    
    @Test
    void testPlainUrl() {
        TelegramMarkdownV2Formatter formatter = new TelegramMarkdownV2Formatter();
        
        String input = "Visit https://example.com for more";
        String result = formatter.format(input);
        
        // Plain URLs should remain unchanged - no special characters to escape
        String expected = "Visit https://example.com for more";
        assertEquals(expected, result);
    }
    
    @Test
    void testDateWithDashes() {
        TelegramMarkdownV2Formatter formatter = new TelegramMarkdownV2Formatter();
        
        String input = "Date: 2025-11-19";
        String result = formatter.format(input);
        
        String expected = "Date: 2025-11-19";
        assertEquals(expected, result);
    }
    
    @Test
    void testMixedContent() {
        TelegramMarkdownV2Formatter formatter = new TelegramMarkdownV2Formatter();
        
        String input = "[Event Name](https://t.me/bot/app?start=123)\n2025-11-19\n\nLink: https://t.me/bot/app";
        String result = formatter.format(input);
        
        String expected = "[Event Name](https://t.me/bot/app?start=123)\n2025-11-19\n\nLink: https://t.me/bot/app";
        assertEquals(expected, result);
    }
    
    @Test
    void testTemplateVariableLikeContent() {
        TelegramMarkdownV2Formatter formatter = new TelegramMarkdownV2Formatter();
        
        // This simulates what the template engine might produce
        String input = "[Nino](https://t.me/bot/app?start=123)\n2025-11-19\n\nLink: https://t.me/bot/app";
        String result = formatter.format(input);
        
        System.out.println("Input: " + input);
        System.out.println("Output: " + result);
        
        // This is what we expect to see in the final message
        String expected = "[Nino](https://t.me/bot/app?start=123)\n2025-11-19\n\nLink: https://t.me/bot/app";
        assertEquals(expected, result);
    }
    
    @Test
    void testTextNeedingEscaping() {
        TelegramMarkdownV2Formatter formatter = new TelegramMarkdownV2Formatter();
        
        String input = "Text with *bold* and _italic_ and [brackets] and (parens)";
        String result = formatter.format(input);
        
        // Only bold and italic should be escaped, brackets and parentheses remain unescaped
        String expected = "Text with \\*bold\\* and \\_italic\\_ and [brackets] and (parens)";
        assertEquals(expected, result);
    }
}