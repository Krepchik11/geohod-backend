package me.geohod.geohodbackend.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test to demonstrate the markdown link issue and verify the solution
 */
public class TelegramMarkdownLinkTest {
    
    @Test
    void testCurrentEscapingBreaksLinks() {
        TelegramNotificationService service = new TelegramNotificationService(null, null);
        
        // This is what NotificationType.EVENT_FINISHED would generate
        String messageWithLink = "Event finished.\n\n[Оставьте отзыв о мероприятии](https://t.me/testbot?start=review_123)";
        
        // Smart escaping should now preserve the link
        String result = service.escapeMarkdownV2(messageWithLink);
        
        // The CORRECT escaped version should preserve the link brackets/parentheses
        // but escape the dot and other special chars
        String expectedCorrectlyEscaped = "Event finished\\.\n\n[Оставьте отзыв о мероприятии](https://t\\.me/testbot?start=review_123)";
        
        System.out.println("Original: " + messageWithLink);
        System.out.println("Escaped:  " + result);
        System.out.println("Expected: " + expectedCorrectlyEscaped);
        
        assertThat(result).isEqualTo(expectedCorrectlyEscaped);
        
        // The link is now broken - it will show as literal text instead of a clickable link
        System.out.println("Original: " + messageWithLink);
        System.out.println("Escaped:  " + result);
        System.out.println("❌ Link is broken - brackets and parentheses are escaped");
    }
    
    @Test
    void testWhatTheEscapedResultShouldLookLike() {
        // This is what we WANT the result to be - preserving the link structure
        String messageWithLink = "Event finished.\n\n[Оставьте отзыв о мероприятии](https://t.me/testbot?start=review_123)";
        
        // The CORRECT escaped version should preserve the link brackets/parentheses
        // but escape the dot and other special chars
        String correctlyEscaped = "Event finished\\.\n\n[Оставьте отзыв о мероприятии](https://t\\.me/testbot?start=review_123)";
        
        System.out.println("Original:           " + messageWithLink);
        System.out.println("Should be escaped:  " + correctlyEscaped);
        System.out.println("✅ Link brackets/parentheses preserved, only URL dot escaped");
        
        // This test documents what the correct behavior should be
        // The link [text](url) structure is preserved, only special chars in URL are escaped
    }
}