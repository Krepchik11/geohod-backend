package me.geohod.geohodbackend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.data.model.repository.UserRepository;
import me.geohod.geohodbackend.service.ITelegramBotService;
import me.geohod.geohodbackend.service.impl.TelegramNotificationService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Telegram Notification Service Tests")
class TelegramNotificationServiceTest {

    private static final Long VALID_CHAT_ID = 123456789L;
    private static final String VALID_CHAT_ID_STRING = "123456789";
    private static final UUID VALID_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID NON_EXISTENT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Mock
    private ITelegramBotService telegramBotService;

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<Long> chatIdCaptor;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private TelegramNotificationService telegramNotificationService;

    @BeforeEach
    void setUp() {
        telegramNotificationService = new TelegramNotificationService(telegramBotService, userRepository);
    }

    @Nested
    @DisplayName("Send Notification Tests")
    class SendNotificationTests {

        @Test
        @DisplayName("Should send notification successfully when user exists and message is valid")
        void shouldSendNotificationSuccessfullyWhenUserExistsAndMessageIsValid() {
            // Given
            User user = createValidUser();
            String message = "Test notification message";
            givenUserExists(user);

            // When
            telegramNotificationService.sendNotification(VALID_USER_ID, message);

            // Then
            then(telegramBotService).should(times(1)).sendMessage(chatIdCaptor.capture(), messageCaptor.capture());
            assertThat(chatIdCaptor.getValue()).isEqualTo(VALID_CHAT_ID);
            assertThat(messageCaptor.getValue()).isEqualTo(message);
        }

        @Test
        @DisplayName("Should escape markdown characters in message before sending")
        void shouldEscapeMarkdownCharactersInMessageBeforeSending() {
            // Given
            User user = createValidUser();
            String messageWithMarkdown = "Event *title* with _special_ [characters]";
            String expectedEscaped = "Event \\*title\\* with \\_special\\_ \\[characters\\]";
            givenUserExists(user);

            // When
            telegramNotificationService.sendNotification(VALID_USER_ID, messageWithMarkdown);

            // Then
            then(telegramBotService).should(times(1)).sendMessage(anyLong(), messageCaptor.capture());
            assertThat(messageCaptor.getValue()).isEqualTo(expectedEscaped);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should handle null and empty messages gracefully")
        void shouldHandleNullAndEmptyMessagesGracefully(String message) {
            // Given
            User user = createValidUser();
            givenUserExists(user);

            // When
            telegramNotificationService.sendNotification(VALID_USER_ID, message);

            // Then
            then(telegramBotService).should(times(1)).sendMessage(VALID_CHAT_ID, message);
        }
    }

    @Nested
    @DisplayName("Markdown Escaping Tests")
    class MarkdownEscapingTests {

        @ParameterizedTest
        @CsvSource({
            "_, \\_",
            "*, \\*",
            "[, \\[",
            "], \\]",
            "(, \\(",
            "), \\)",
            "~, \\~",
            "`, \\`",
            ">, \\>",
            "#, \\#",
            "+, \\+",
            "-, \\-",
            "=, \\=",
            "|, \\|",
            "{, \\{",
            "}, \\}",
            "., \\.",
            "!, \\!"
        })
        @DisplayName("Should escape individual markdown special characters")
        void shouldEscapeIndividualMarkdownSpecialCharacters(String input, String expected) {
            // When
            String result = telegramNotificationService.escapeMarkdownV2(input);

            // Then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should return null when input is null")
        void shouldReturnNullWhenInputIsNull() {
            // When
            String result = telegramNotificationService.escapeMarkdownV2(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return empty string when input is empty")
        void shouldReturnEmptyStringWhenInputIsEmpty() {
            // When
            String result = telegramNotificationService.escapeMarkdownV2("");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should escape multiple special characters in complex message")
        void shouldEscapeMultipleSpecialCharactersInComplexMessage() {
            // Given
            String complexMessage = "Check out this *amazing* deal: [50% off](https://example.com)!";
            String expected = "Check out this \\*amazing\\* deal: \\[50% off\\]\\(https://example\\.com\\)\\!";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(complexMessage);

            // Then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should handle unicode characters correctly")
        void shouldHandleUnicodeCharactersCorrectly() {
            // Given
            String unicodeMessage = "Привет *мир*! Hello _world_!";
            String expected = "Привет \\*мир\\*\\! Hello \\_world\\_\\!";

            // When
            String result = telegramNotificationService.escapeMarkdownV2(unicodeMessage);

            // Then
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when user not found")
        void shouldThrowIllegalArgumentExceptionWhenUserNotFound() {
            // Given
            givenUserDoesNotExist();

            // When & Then
            assertThatThrownBy(() -> telegramNotificationService.sendNotification(NON_EXISTENT_USER_ID, "Test message"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: " + NON_EXISTENT_USER_ID);
            
            then(telegramBotService).should(never()).sendMessage(anyLong(), anyString());
        }

        @Test
        @DisplayName("Should throw NumberFormatException when telegram ID is not numeric")
        void shouldThrowNumberFormatExceptionWhenTelegramIdIsNotNumeric() {
            // Given
            User userWithInvalidTgId = createUserWithInvalidTelegramId();
            givenUserExists(userWithInvalidTgId);

            // When & Then
            assertThatThrownBy(() -> telegramNotificationService.sendNotification(VALID_USER_ID, "Test message"))
                .isInstanceOf(NumberFormatException.class);
            
            then(telegramBotService).should(never()).sendMessage(anyLong(), anyString());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "abc", "123abc", "abc123", "12.34"})
        @DisplayName("Should throw NumberFormatException for various invalid telegram ID formats")
        void shouldThrowNumberFormatExceptionForInvalidTelegramIdFormats(String invalidTgId) {
            // Given
            User user = createUserWithTelegramId(invalidTgId);
            givenUserExists(user);

            // When & Then
            assertThatThrownBy(() -> telegramNotificationService.sendNotification(VALID_USER_ID, "Test message"))
                .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("Should handle negative telegram ID correctly")
        void shouldHandleNegativeTelegramIdCorrectly() {
            // Given
            User user = createUserWithTelegramId("-123");
            givenUserExists(user);

            // When
            telegramNotificationService.sendNotification(VALID_USER_ID, "Test message");

            // Then
            then(telegramBotService).should(times(1)).sendMessage(-123L, "Test message");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very long messages")
        void shouldHandleVeryLongMessages() {
            // Given
            User user = createValidUser();
            String longMessage = "A".repeat(1000);
            givenUserExists(user);

            // When
            telegramNotificationService.sendNotification(VALID_USER_ID, longMessage);

            // Then
            then(telegramBotService).should(times(1)).sendMessage(VALID_CHAT_ID, longMessage);
        }

        @Test
        @DisplayName("Should handle messages with only special characters")
        void shouldHandleMessagesWithOnlySpecialCharacters() {
            // Given
            User user = createValidUser();
            String specialChars = "*_[]()~`>#+-=|{}.!";
            String expectedEscaped = "\\*\\_\\[\\]\\(\\)\\~\\`\\>\\#\\+\\-\\=\\|\\{\\}\\.\\!";
            givenUserExists(user);

            // When
            telegramNotificationService.sendNotification(VALID_USER_ID, specialChars);

            // Then
            then(telegramBotService).should(times(1)).sendMessage(VALID_CHAT_ID, expectedEscaped);
        }
    }

    // Helper methods
    private User createValidUser() {
        return createUserWithTelegramId(VALID_CHAT_ID_STRING);
    }

    private User createUserWithTelegramId(String tgId) {
        return new User(
            tgId,
            "test_user",
            "Test",
            "User",
            "http://example.com/avatar.jpg"
        );
    }

    private User createUserWithInvalidTelegramId() {
        return createUserWithTelegramId("invalid_telegram_id");
    }

    private void givenUserExists(User user) {
        when(userRepository.findById(VALID_USER_ID)).thenReturn(Optional.of(user));
    }

    private void givenUserDoesNotExist() {
        when(userRepository.findById(NON_EXISTENT_USER_ID)).thenReturn(Optional.empty());
    }
}
