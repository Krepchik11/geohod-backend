package me.geohod.geohodbackend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.data.model.repository.EventParticipantRepository;
import me.geohod.geohodbackend.data.model.repository.UserRepository;
import me.geohod.geohodbackend.service.ITelegramOutboxMessagePublisher;
import me.geohod.geohodbackend.service.IUserService;
import me.geohod.geohodbackend.service.notification.processor.strategy.EventCancelledParticipantTelegramStrategy;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.MessageFormatter;
import me.geohod.geohodbackend.service.notification.processor.strategy.message.TemplateType;

@ExtendWith(MockitoExtension.class)
class EventCancelledParticipantTelegramStrategyTest {

    @Mock
    private GeohodProperties properties;

    @Mock
    private EventParticipantRepository eventParticipantRepository;

    @Mock
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MessageFormatter messageFormatter;

    @Mock
    private ITelegramOutboxMessagePublisher telegramOutboxMessagePublisher;

    @Mock
    private IUserService userService;

    private EventCancelledParticipantTelegramStrategy strategy;

    private Event event;
    private UUID eventId;
    private UUID authorId;
    private UUID participantId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        participantId = UUID.randomUUID();

        event = new Event("Test Event", "Description", java.time.Instant.now(), 10, authorId);
        strategy = new EventCancelledParticipantTelegramStrategy(
            properties, eventParticipantRepository, userRepository,
            objectMapper, messageFormatter, telegramOutboxMessagePublisher, userService
        );
    }

    @Test
    void testSendNotificationToParticipantsOnly() throws Exception {
        String payload = "{\"notifyParticipants\": true}";
        EventParticipant participant = new EventParticipant(eventId, participantId);
        User author = new User("123456", "test_author", "Test", "Author", null);

        when(eventParticipantRepository.findEventParticipantByEventId(event.getId()))
            .thenReturn(java.util.List.of(participant));
        when(userService.getUser(authorId)).thenReturn(author);

        GeohodProperties.LinkTemplates linkTemplates = mock(GeohodProperties.LinkTemplates.class);
        when(linkTemplates.startappLink()).thenReturn("https://t.me/testbot?startapp=");
        when(properties.linkTemplates()).thenReturn(linkTemplates);

        when(messageFormatter.formatMessageFromTemplate(
            anyString(),
            any(TemplateType.class),
            any(Event.class),
            any(User.class),
            any()))
            .thenReturn("Event was cancelled");

        strategy.send(event, payload);

        verify(telegramOutboxMessagePublisher, times(1)).publish(eq(participantId), eq("Event was cancelled"));
        verify(telegramOutboxMessagePublisher, never()).publish(eq(authorId), anyString());
    }

    @Test
    void testSkipWhenNotifyParticipantsIsFalse() throws Exception {
        String payload = "{\"notifyParticipants\": false}";

        strategy.send(event, payload);

        verifyNoInteractions(telegramOutboxMessagePublisher);
    }
}

