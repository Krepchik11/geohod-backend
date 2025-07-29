package me.geohod.geohodbackend.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.data.model.repository.UserRepository;
import me.geohod.geohodbackend.service.ITelegramBotService;

@ExtendWith(MockitoExtension.class)
@DisplayName("TelegramNotificationService Tests")
class TelegramNotificationServiceTest {

    @Mock
    private ITelegramBotService telegramBotService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TelegramNotificationService telegramNotificationService;

    @Nested
    @DisplayName("escapeMarkdownV2 Tests")
    class EscapeMarkdownV2Tests {

        @Test
        @DisplayName("should return null for null input")
        void shouldReturnNullForNullInput() {
            // When
            String result = telegramNotificationService.escapeMarkdownV2(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should return empty string for empty input")
        void shouldReturnEmptyStringForEmptyInput() {
            // When
            String result = telegramNotificationService.escapeMarkdownV2("");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should escape backslashes first")
        void shouldEscapeBackslashesFirst() {
            // Given
            String input = "Text with \\ backslash";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo("Text with \\\\ backslash");
        }

        @Test
        @DisplayName("should escape all special MarkdownV2 characters")
        void shouldEscapeAllSpecialMarkdownV2Characters() {
            // Given
            String input = "_*[]()~`>#+-=|{}.!";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then - Empty [] and () are escaped since they don't form valid markdown links
            assertThat(result).isEqualTo("\\_\\*\\[\\]\\(\\)\\~\\`\\>\\#\\+\\-\\=\\|\\{\\}\\.\\!");
        }

        @Test
        @DisplayName("should handle mixed text with special characters")
        void shouldHandleMixedTextWithSpecialCharacters() {
            // Given
            String input = "Hello *world*! How are you [today]?";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo("Hello \\*world\\*\\! How are you \\[today\\]?");
        }

        @Test
        @DisplayName("should escape backslashes before other special characters")
        void shouldEscapeBackslashesBeforeOtherSpecialCharacters() {
            // Given
            String input = "\\*bold text\\*";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo("\\\\\\*bold text\\\\\\*");
        }

        @Test
        @DisplayName("should preserve markdown links while escaping other text")
        void shouldPreserveMarkdownLinksWhileEscapingOtherText() {
            // Given
            String input = "[Link](https://example.com) with *bold* and _italic_ text!";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo("[Link](https://example\\.com) with \\*bold\\* and \\_italic\\_ text\\!");
        }

        @Test
        @DisplayName("should handle Russian text with special characters")
        void shouldHandleRussianTextWithSpecialCharacters() {
            // Given
            String input = "Организатор отменил мероприятие *Java Meetup* (2024-01-15)!";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo("Организатор отменил мероприятие \\*Java Meetup\\* \\(2024\\-01\\-15\\)\\!");
        }

        @Test
        @DisplayName("should handle text with multiple consecutive special characters")
        void shouldHandleTextWithMultipleConsecutiveSpecialCharacters() {
            // Given
            String input = "Text with --- and ... and !!!";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo("Text with \\-\\-\\- and \\.\\.\\. and \\!\\!\\!");
        }

        @Test
        @DisplayName("should handle text with code blocks")
        void shouldHandleTextWithCodeBlocks() {
            // Given
            String input = "Use `console.log()` to debug";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo("Use \\`console\\.log\\(\\)\\` to debug");
        }

        @Test
        @DisplayName("should handle text without special characters unchanged")
        void shouldHandleTextWithoutSpecialCharactersUnchanged() {
            // Given
            String input = "Simple text without special characters";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo("Simple text without special characters");
        }

        @Test
        @DisplayName("should handle edge case with only special characters")
        void shouldHandleEdgeCaseWithOnlySpecialCharacters() {
            // Given
            String input = "!@#$%^&*()";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo("\\!@\\#$%^&\\*\\(\\)");
        }

        @Test
        @DisplayName("should preserve valid markdown links")
        void shouldPreserveValidMarkdownLinks() {
            // Given
            String input = "Check out [Google](https://google.com) and [GitHub](https://github.com)!";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo("Check out [Google](https://google\\.com) and [GitHub](https://github\\.com)\\!");
        }

        @Test
        @DisplayName("should escape brackets when not part of valid link")
        void shouldEscapeBracketsWhenNotPartOfValidLink() {
            // Given
            String input = "Array[0] and function() call";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo("Array\\[0\\] and function\\(\\) call");
        }

        @Test
        @DisplayName("should handle markdown links with special characters in URL")
        void shouldHandleMarkdownLinksWithSpecialCharactersInUrl() {
            // Given
            String input = "[Review](https://t.me/bot?start=review_123) form";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo("[Review](https://t\\.me/bot?start=review_123) form");
        }

        @Test
        @DisplayName("should handle markdown links with escaped characters in link text")
        void shouldHandleMarkdownLinksWithEscapedCharactersInLinkText() {
            // Given
            String input = "[*Bold* Link!](https://example.com) text";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo("[\\*Bold\\* Link\\!](https://example\\.com) text");
        }

        @Test
        @DisplayName("should handle text with both links and regular parentheses")
        void shouldHandleTextWithBothLinksAndRegularParentheses() {
            // Given
            String input = "Event (2024-01-15) with [link](https://example.com) and more (info)";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo("Event \\(2024\\-01\\-15\\) with [link](https://example\\.com) and more \\(info\\)");
        }

        @Test
        @DisplayName("should handle real notification message with review link")
        void shouldHandleRealNotificationMessageWithReviewLink() {
            // Given
            String input = "Мероприятие Tech Conference 2024 (2024-01-15) завершено.\nОрганизатор: Jennifer Lee @jenlee\n\nСредний размер доната: 300 руб.\n\n[Оставьте отзыв о мероприятии](https://t.me/testbot?start=review_550e8400-e29b-41d4-a716-446655440000)";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            String expected = "Мероприятие Tech Conference 2024 \\(2024\\-01\\-15\\) завершено\\.\nОрганизатор: Jennifer Lee @jenlee\n\nСредний размер доната: 300 руб\\.\n\n[Оставьте отзыв о мероприятии](https://t\\.me/testbot?start=review_550e8400\\-e29b\\-41d4\\-a716\\-446655440000)";
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should handle incomplete markdown syntax")
        void shouldHandleIncompleteMarkdownSyntax() {
            // Given
            String input = "[incomplete link without closing bracket and (incomplete parentheses";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo("\\[incomplete link without closing bracket and \\(incomplete parentheses");
        }
    }

    @Nested
    @DisplayName("sendNotification Tests")
    class SendNotificationTests {

        private User testUser;
        private UUID userId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            testUser = new User("123456789", "testuser", "Test", "User", "https://example.com/avatar.jpg");
        }

        @Test
        @DisplayName("should send escaped notification message successfully preserving links")
        void shouldSendEscapedNotificationMessageSuccessfullyPreservingLinks() {
            // Given
            String originalMessage = "Hello *world*! This is a [test](https://example.com).";
            String expectedEscapedMessage = "Hello \\*world\\*\\! This is a [test](https://example\\.com)\\.";
            
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            telegramNotificationService.sendNotification(userId, originalMessage);

            // Then
            verify(userRepository).findById(userId);
            verify(telegramBotService).sendMessage(123456789L, expectedEscapedMessage);
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            String message = "Test message";
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> telegramNotificationService.sendNotification(userId, message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found: " + userId);

            verify(userRepository).findById(userId);
            verify(telegramBotService, never()).sendMessage(anyLong(), anyString());
        }

        @Test
        @DisplayName("should handle null message gracefully")
        void shouldHandleNullMessageGracefully() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            telegramNotificationService.sendNotification(userId, null);

            // Then
            verify(userRepository).findById(userId);
            verify(telegramBotService).sendMessage(123456789L, null);
        }

        @Test
        @DisplayName("should handle empty message")
        void shouldHandleEmptyMessage() {
            // Given
            String emptyMessage = "";
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            telegramNotificationService.sendNotification(userId, emptyMessage);

            // Then
            verify(userRepository).findById(userId);
            verify(telegramBotService).sendMessage(123456789L, "");
        }
    }
}