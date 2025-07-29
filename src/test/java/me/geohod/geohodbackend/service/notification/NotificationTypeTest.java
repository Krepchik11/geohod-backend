package me.geohod.geohodbackend.service.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.User;

@DisplayName("NotificationType Message Formatting Tests")
class NotificationTypeTest {

    // Test Data Builders
    
    private static Event createTestEvent(String name, Instant date) {
        return new Event(name, "Test description", date, 10, UUID.randomUUID());
    }
    
    private static User createTestUser(String firstName, String lastName, String tgUsername) {
        return new User("123456789", tgUsername, firstName, lastName, "https://example.com/avatar.jpg");
    }
    
    private static EventContext createEventContext(Event event, User user) {
        return new EventContext(event, user);
    }
    
    private static NotificationParams createEventCreatedParams() {
        return NotificationParams.eventCreatedParams(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                "testbot",
                "https://t.me/{botName}?start=event_{eventId}"
        );
    }
    
    private static NotificationParams createEventFinishedParams(String donationInfo, boolean sendPollLink) {
        if (sendPollLink) {
            return NotificationParams.eventFinishedParams(
                    donationInfo,
                    true,
                    UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                    "testbot",
                    "https://t.me/{botName}?start=review_{eventId}"
            );
        }
        return NotificationParams.eventFinishedParams(donationInfo);
    }
    
    private static String formatEventDate(Instant eventDate) {
        return LocalDate.ofInstant(eventDate, ZoneId.systemDefault()).toString();
    }

    // Common test data
    private final Instant testDate = Instant.parse("2024-01-15T10:00:00Z");
    private final String expectedFormattedDate = formatEventDate(testDate);

    @Nested
    @DisplayName("EVENT_CANCELLED Tests")
    class EventCancelledTests {

        @Test
        @DisplayName("should format message with full contact info")
        void shouldFormatMessageWithFullContactInfo() {
            // Given
            Event event = createTestEvent("Java Meetup", testDate);
            User user = createTestUser("John", "Doe", "johndoe");
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.EVENT_CANCELLED.formatMessage(context, params);

            // Then
            String expected = String.format(
                    "Организатор отменил мероприятие Java Meetup (%s)\nОрганизатор: John Doe @johndoe",
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should format message with only first name")
        void shouldFormatMessageWithOnlyFirstName() {
            // Given
            Event event = createTestEvent("Spring Boot Workshop", testDate);
            User user = createTestUser("Alice", null, "alice");
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.EVENT_CANCELLED.formatMessage(context, params);

            // Then
            String expected = String.format(
                    "Организатор отменил мероприятие Spring Boot Workshop (%s)\nОрганизатор: Alice @alice",
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should format message with only username")
        void shouldFormatMessageWithOnlyUsername() {
            // Given
            Event event = createTestEvent("Docker Training", testDate);
            User user = createTestUser(null, null, "dockerfan");
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.EVENT_CANCELLED.formatMessage(context, params);

            // Then
            String expected = String.format(
                    "Организатор отменил мероприятие Docker Training (%s)\nОрганизатор: @dockerfan",
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should format message without contact info when none available")
        void shouldFormatMessageWithoutContactInfo() {
            // Given
            Event event = createTestEvent("Kubernetes Basics", testDate);
            User user = createTestUser(null, null, null);
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.EVENT_CANCELLED.formatMessage(context, params);

            // Then
            String expected = String.format(
                    "Организатор отменил мероприятие Kubernetes Basics (%s)",
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should handle event names with special characters")
        void shouldHandleEventNamesWithSpecialCharacters() {
            // Given
            Event event = createTestEvent("C++ & Java: Advanced Topics", testDate);
            User user = createTestUser("Dev", "Expert", "devexpert");
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.EVENT_CANCELLED.formatMessage(context, params);

            // Then
            String expected = String.format(
                    "Организатор отменил мероприятие C++ & Java: Advanced Topics (%s)\nОрганизатор: Dev Expert @devexpert",
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("PARTICIPANT_REGISTERED Tests")
    class ParticipantRegisteredTests {

        @Test
        @DisplayName("should format registration message with contact info")
        void shouldFormatRegistrationMessageWithContactInfo() {
            // Given
            Event event = createTestEvent("React Workshop", testDate);
            User user = createTestUser("Sarah", "Wilson", "sarahw");
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.PARTICIPANT_REGISTERED.formatMessage(context, params);

            // Then
            String expected = String.format("""
                            Вы зарегистрировались на мероприятие React Workshop (%s)
                            Организатор: Sarah Wilson @sarahw
                            """,
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should format registration message without contact info")
        void shouldFormatRegistrationMessageWithoutContactInfo() {
            // Given
            Event event = createTestEvent("Vue.js Fundamentals", testDate);
            User user = createTestUser(null, null, null);
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.PARTICIPANT_REGISTERED.formatMessage(context, params);

            // Then
            String expected = String.format("""
                            Вы зарегистрировались на мероприятие Vue.js Fundamentals (%s)
                            
                            """,
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should format registration message with only full name")
        void shouldFormatRegistrationMessageWithOnlyFullName() {
            // Given
            Event event = createTestEvent("Angular Deep Dive", testDate);
            User user = createTestUser("Mike", "Johnson", null);
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.PARTICIPANT_REGISTERED.formatMessage(context, params);

            // Then
            String expected = String.format("""
                            Вы зарегистрировались на мероприятие Angular Deep Dive (%s)
                            Организатор: Mike Johnson
                            """,
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("PARTICIPANT_UNREGISTERED Tests")
    class ParticipantUnregisteredTests {

        @Test
        @DisplayName("should format unregistration message")
        void shouldFormatUnregistrationMessage() {
            // Given
            Event event = createTestEvent("Node.js Masterclass", testDate);
            User user = createTestUser("Emma", "Brown", "emmab");
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.PARTICIPANT_UNREGISTERED.formatMessage(context, params);

            // Then
            String expected = String.format("""
                            Вы отменили регистрацию на мероприятие Node.js Masterclass (%s)
                            """,
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should format unregistration message for event with long name")
        void shouldFormatUnregistrationMessageForEventWithLongName() {
            // Given
            Event event = createTestEvent("Full-Stack JavaScript Development: From Frontend to Backend", testDate);
            User user = createTestUser("Alex", "Smith", "alexs");
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.PARTICIPANT_UNREGISTERED.formatMessage(context, params);

            // Then
            String expected = String.format("""
                            Вы отменили регистрацию на мероприятие Full-Stack JavaScript Development: From Frontend to Backend (%s)
                            """,
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("EVENT_CREATED Tests")
    class EventCreatedTests {

        @Test
        @DisplayName("should format event creation message with registration link and contact info")
        void shouldFormatEventCreationMessageWithRegistrationLinkAndContactInfo() {
            // Given
            Event event = createTestEvent("Python for Beginners", testDate);
            User user = createTestUser("Dr. Anna", "Smith", "annasmith");
            EventContext context = createEventContext(event, user);
            NotificationParams params = createEventCreatedParams();

            // When
            String result = NotificationType.EVENT_CREATED.formatMessage(context, params);

            // Then
            String expected = String.format("""
                            Вы создали мероприятие:
                            
                            Python for Beginners
                            %s
                            Организатор: Dr. Anna Smith @annasmith
                            
                            [Ссылка на регистрацию](https://t.me/testbot?start=event_550e8400-e29b-41d4-a716-446655440000)""",
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should format event creation message without contact info")
        void shouldFormatEventCreationMessageWithoutContactInfo() {
            // Given
            Event event = createTestEvent("Machine Learning Basics", testDate);
            User user = createTestUser(null, null, null);
            EventContext context = createEventContext(event, user);
            NotificationParams params = createEventCreatedParams();

            // When
            String result = NotificationType.EVENT_CREATED.formatMessage(context, params);

            // Then
            String expected = String.format("""
                            Вы создали мероприятие:
                            
                            Machine Learning Basics
                            %s
                            
                            
                            [Ссылка на регистрацию](https://t.me/testbot?start=event_550e8400-e29b-41d4-a716-446655440000)""",
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should correctly replace bot name and event ID in link template")
        void shouldCorrectlyReplaceBotNameAndEventIdInLinkTemplate() {
            // Given
            Event event = createTestEvent("AI Workshop", testDate);
            User user = createTestUser("Prof.", "Johnson", "profjohnson");
            EventContext context = createEventContext(event, user);
            
            NotificationParams params = new NotificationParams(
                    UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                    null,
                    "myspecialbot",
                    "https://telegram.me/{botName}/start?event={eventId}",
                    false
            );

            // When
            String result = NotificationType.EVENT_CREATED.formatMessage(context, params);

            // Then
            assertThat(result).contains("https://telegram.me/myspecialbot/start?event=123e4567-e89b-12d3-a456-426614174000");
        }
    }

    @Nested
    @DisplayName("EVENT_FINISHED Tests")
    class EventFinishedTests {

        @Test
        @DisplayName("should format finished message with basic info only")
        void shouldFormatFinishedMessageWithBasicInfoOnly() {
            // Given
            Event event = createTestEvent("Database Design Workshop", testDate);
            User user = createTestUser("Tom", "Davis", "tomdavis");
            EventContext context = createEventContext(event, user);
            NotificationParams params = createEventFinishedParams(null, false);

            // When
            String result = NotificationType.EVENT_FINISHED.formatMessage(context, params);

            // Then
            String expected = String.format("""
                            Мероприятие Database Design Workshop (%s) завершено.
                            Организатор: Tom Davis @tomdavis""",
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should format finished message with donation info")
        void shouldFormatFinishedMessageWithDonationInfo() {
            // Given
            Event event = createTestEvent("Charity Coding Marathon", testDate);
            User user = createTestUser("Lisa", "Anderson", "lisaa");
            EventContext context = createEventContext(event, user);
            NotificationParams params = createEventFinishedParams("150 руб.", false);

            // When
            String result = NotificationType.EVENT_FINISHED.formatMessage(context, params);

            // Then
            String expected = String.format("""
                            Мероприятие Charity Coding Marathon (%s) завершено.
                            Организатор: Lisa Anderson @lisaa
                            
                            Средний размер доната: 150 руб.""",
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should format finished message with poll link")
        void shouldFormatFinishedMessageWithPollLink() {
            // Given
            Event event = createTestEvent("Code Review Session", testDate);
            User user = createTestUser("Mark", "Wilson", "markw");
            EventContext context = createEventContext(event, user);
            NotificationParams params = createEventFinishedParams(null, true);

            // When
            String result = NotificationType.EVENT_FINISHED.formatMessage(context, params);

            // Then
            String expected = String.format("""
                            Мероприятие Code Review Session (%s) завершено.
                            Организатор: Mark Wilson @markw

                            [Оставьте отзыв о мероприятии](https://t.me/testbot?start=review_550e8400-e29b-41d4-a716-446655440000)""",
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should format finished message with donation info and poll link")
        void shouldFormatFinishedMessageWithDonationInfoAndPollLink() {
            // Given
            Event event = createTestEvent("Tech Conference 2024", testDate);
            User user = createTestUser("Jennifer", "Lee", "jenlee");
            EventContext context = createEventContext(event, user);
            NotificationParams params = createEventFinishedParams("300 руб.", true);

            // When
            String result = NotificationType.EVENT_FINISHED.formatMessage(context, params);

            // Then
            String expected = String.format("""
                            Мероприятие Tech Conference 2024 (%s) завершено.
                            Организатор: Jennifer Lee @jenlee
                            
                            Средний размер доната: 300 руб.

                            [Оставьте отзыв о мероприятии](https://t.me/testbot?start=review_550e8400-e29b-41d4-a716-446655440000)""",
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should not include donation info when donation string is blank")
        void shouldNotIncludeDonationInfoWhenDonationStringIsBlank() {
            // Given
            Event event = createTestEvent("Free Coding Workshop", testDate);
            User user = createTestUser("Peter", "Brown", "peterb");
            EventContext context = createEventContext(event, user);
            NotificationParams params = createEventFinishedParams("", false);

            // When
            String result = NotificationType.EVENT_FINISHED.formatMessage(context, params);

            // Then
            String expected = String.format("""
                            Мероприятие Free Coding Workshop (%s) завершено.
                            Организатор: Peter Brown @peterb""",
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should not include donation info when donation string contains only whitespace")
        void shouldNotIncludeDonationInfoWhenDonationStringContainsOnlyWhitespace() {
            // Given
            Event event = createTestEvent("Open Source Contribution", testDate);
            User user = createTestUser("Carol", "White", "carolw");
            EventContext context = createEventContext(event, user);
            NotificationParams params = createEventFinishedParams("   ", false);

            // When
            String result = NotificationType.EVENT_FINISHED.formatMessage(context, params);

            // Then
            String expected = String.format("""
                            Мероприятие Open Source Contribution (%s) завершено.
                            Организатор: Carol White @carolw""",
                    expectedFormattedDate
            );
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Date Formatting Tests")
    class DateFormattingTests {

        @Test
        @DisplayName("should format date consistently across all notification types")
        void shouldFormatDateConsistentlyAcrossAllNotificationTypes() {
            // Given
            Instant specificDate = Instant.parse("2024-12-25T14:30:00Z");
            String expectedDate = LocalDate.ofInstant(specificDate, ZoneId.systemDefault()).toString();
            
            Event event = createTestEvent("Christmas Coding", specificDate);
            User user = createTestUser("Santa", "Claus", "santa");
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When & Then - Test each notification type contains the same formatted date
            String cancelledMessage = NotificationType.EVENT_CANCELLED.formatMessage(context, params);
            String registeredMessage = NotificationType.PARTICIPANT_REGISTERED.formatMessage(context, params);
            String unregisteredMessage = NotificationType.PARTICIPANT_UNREGISTERED.formatMessage(context, params);
            String createdMessage = NotificationType.EVENT_CREATED.formatMessage(context, createEventCreatedParams());
            String finishedMessage = NotificationType.EVENT_FINISHED.formatMessage(context, params);

            assertThat(cancelledMessage).contains(String.format("(%s)", expectedDate));
            assertThat(registeredMessage).contains(String.format("(%s)", expectedDate));
            assertThat(unregisteredMessage).contains(String.format("(%s)", expectedDate));
            assertThat(createdMessage).contains(expectedDate);
            assertThat(finishedMessage).contains(String.format("(%s)", expectedDate));
        }

        @Test
        @DisplayName("should handle different time zones correctly")
        void shouldHandleDifferentTimeZonesCorrectly() {
            // Given
            Instant utcDate = Instant.parse("2024-06-15T23:30:00Z");
            String expectedDate = LocalDate.ofInstant(utcDate, ZoneId.systemDefault()).toString();
            
            Event event = createTestEvent("Late Night Coding", utcDate);
            User user = createTestUser("Night", "Owl", "nightowl");
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.EVENT_CANCELLED.formatMessage(context, params);

            // Then
            assertThat(result).contains(String.format("(%s)", expectedDate));
        }

        @Test
        @DisplayName("should handle edge case dates correctly")
        void shouldHandleEdgeCaseDatesCorrectly() {
            // Given - Test with epoch start date
            Instant epochDate = Instant.ofEpochSecond(0);
            String expectedDate = LocalDate.ofInstant(epochDate, ZoneId.systemDefault()).toString();
            
            Event event = createTestEvent("Unix Epoch Celebration", epochDate);
            User user = createTestUser("Unix", "Admin", "unixadmin");
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.EVENT_CANCELLED.formatMessage(context, params);

            // Then
            assertThat(result).contains(String.format("(%s)", expectedDate));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCasesAndBoundaryConditions {

        @Test
        @DisplayName("should handle empty event name gracefully")
        void shouldHandleEmptyEventNameGracefully() {
            // Given
            Event event = createTestEvent("", testDate);
            User user = createTestUser("Test", "User", "testuser");
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.EVENT_CANCELLED.formatMessage(context, params);

            // Then
            assertThat(result).contains("Организатор отменил мероприятие  (");
            assertThat(result).contains(expectedFormattedDate);
        }

        @Test
        @DisplayName("should handle event names with newlines")
        void shouldHandleEventNamesWithNewlines() {
            // Given
            Event event = createTestEvent("Multi\nLine\nEvent", testDate);
            User user = createTestUser("Multi", "Liner", "multiliner");
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.EVENT_CANCELLED.formatMessage(context, params);

            // Then
            assertThat(result).contains("Multi\nLine\nEvent");
        }

        @Test
        @DisplayName("should handle usernames with special characters")
        void shouldHandleUsernamesWithSpecialCharacters() {
            // Given
            Event event = createTestEvent("Special Event", testDate);
            User user = createTestUser("User", "Name", "user_name_123");
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.EVENT_CANCELLED.formatMessage(context, params);

            // Then
            assertThat(result).contains("@user_name_123");
        }

        @Test
        @DisplayName("should handle very long event names")
        void shouldHandleVeryLongEventNames() {
            // Given
            String longEventName = "A".repeat(200); // Very long event name
            Event event = createTestEvent(longEventName, testDate);
            User user = createTestUser("Long", "Event", "longevent");
            EventContext context = createEventContext(event, user);
            NotificationParams params = NotificationParams.empty();

            // When
            String result = NotificationType.EVENT_CANCELLED.formatMessage(context, params);

            // Then
            assertThat(result).contains(longEventName);
            assertThat(result).contains(expectedFormattedDate);
        }

        @Test
        @DisplayName("should handle link template with no placeholders")
        void shouldHandleLinkTemplateWithNoPlaceholders() {
            // Given
            Event event = createTestEvent("Static Link Event", testDate);
            User user = createTestUser("Static", "User", "staticuser");
            EventContext context = createEventContext(event, user);
            
            NotificationParams params = new NotificationParams(
                    UUID.randomUUID(),
                    null,
                    "bot",
                    "https://example.com/static-link",
                    false
            );

            // When
            String result = NotificationType.EVENT_CREATED.formatMessage(context, params);

            // Then
            assertThat(result).contains("https://example.com/static-link");
        }
    }
}