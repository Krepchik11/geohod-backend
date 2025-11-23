package me.geohod.geohodbackend.data.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import me.geohod.geohodbackend.TestContainersPostgresConfig;
import me.geohod.geohodbackend.data.dto.EventDetailedProjection;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.EventParticipant;
import me.geohod.geohodbackend.data.model.User;

public class EventProjectionRepositoryTest extends TestContainersPostgresConfig {

    @Autowired
    private EventProjectionRepository eventProjectionRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventParticipantRepository eventParticipantRepository;

    private UUID authorId;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        User author = new User("12345", "author", "Author", "User", null);
        userRepository.save(author);
        authorId = author.getId();
    }

    @Test
    void shouldSortEventsByNameAsc() {
        createEvent("C Event", Instant.now());
        createEvent("A Event", Instant.now());
        createEvent("B Event", Instant.now());

        Page<EventDetailedProjection> page = eventProjectionRepository.events(
                authorId, null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));

        assertThat(page.getContent()).extracting(EventDetailedProjection::name)
                .containsExactly("A Event", "B Event", "C Event");
    }

    @Test
    void shouldSortEventsByDateDesc() {
        Instant now = Instant.now();
        createEvent("Event 1", now.minus(1, ChronoUnit.DAYS));
        createEvent("Event 2", now.plus(1, ChronoUnit.DAYS));
        createEvent("Event 3", now);

        Page<EventDetailedProjection> page = eventProjectionRepository.events(
                authorId, null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date")));

        assertThat(page.getContent()).extracting(EventDetailedProjection::name)
                .containsExactly("Event 2", "Event 3", "Event 1");
    }

    @Test
    void shouldSortEventsByStatus() {
        createEvent("Active", Instant.now(), Event.Status.ACTIVE);
        createEvent("Finished", Instant.now(), Event.Status.FINISHED);
        createEvent("Canceled", Instant.now(), Event.Status.CANCELED);

        Page<EventDetailedProjection> page = eventProjectionRepository.events(
                authorId, null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "status")));

        // ACTIVE, CANCELED, FINISHED (alphabetical order of enum names)
        assertThat(page.getContent()).extracting(EventDetailedProjection::status)
                .containsExactly(Event.Status.ACTIVE, Event.Status.CANCELED, Event.Status.FINISHED);
    }

    @Test
    void shouldSortEventsByCreatedAtDefault() {
        createEvent("Event 1", Instant.now());
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
        createEvent("Event 2", Instant.now());
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
        createEvent("Event 3", Instant.now());

        Page<EventDetailedProjection> page = eventProjectionRepository.events(
                authorId, null, null,
                PageRequest.of(0, 10));

        // Default is created_at DESC
        assertThat(page.getContent()).extracting(EventDetailedProjection::name)
                .containsExactly("Event 3", "Event 2", "Event 1");
    }

    private void createEvent(String name, Instant date) {
        createEvent(name, date, Event.Status.ACTIVE);
    }

    private void createEvent(String name, Instant date, Event.Status status) {
        Event event = new Event(name, "Description", date, 10, authorId);
        event = eventRepository.save(event);

        if (status == Event.Status.CANCELED) {
            event.cancel();
            eventRepository.save(event);
        } else if (status == Event.Status.FINISHED) {
            event.finish(false, false, false);
            eventRepository.save(event);
        }
    }

    @Test
    void shouldReturnEventWithActionStates() {
        // Given
        Event event = new Event("Event with states", "Description", Instant.now(), 10, authorId);
        event.finish(true, true, false);
        eventRepository.save(event);

        // When
        EventDetailedProjection projection = eventProjectionRepository.event(event.getId(), null);

        // Then
        assertThat(projection.sendPollLink()).isTrue();
        assertThat(projection.donationCash()).isTrue();
        assertThat(projection.donationTransfer()).isFalse();
    }

    @Test
    void shouldReturnParticipantState() {
        // Given
        Event event = new Event("Event with participant", "Description", Instant.now(), 10, authorId);
        eventRepository.save(event);

        User participantUser = new User("54321", "participant", "Participant", "User", null);
        userRepository.save(participantUser);

        EventParticipant participant = new EventParticipant(
                event.getId(), participantUser.getId());
        participant.updateState(true, false, true);
        eventParticipantRepository.save(participant);

        // When
        EventDetailedProjection projection = eventProjectionRepository.event(event.getId(), participantUser.getId());

        // Then
        assertThat(projection.participantState()).isNotNull();
        assertThat(projection.participantState().pollLinkSent()).isTrue();
        assertThat(projection.participantState().cashDonated()).isFalse();
        assertThat(projection.participantState().transferDonated()).isTrue();
    }

    @Test
    void shouldAggregateParticipantStates() {
        // Given
        Event event = new Event("Event with multiple participants", "Description", Instant.now(), 10, authorId);
        eventRepository.save(event);

        User participantUser = new User("99999", "multi_participant", "Multi", "Participant", null);
        userRepository.save(participantUser);

        // First participation: only poll link sent
        EventParticipant p1 = new EventParticipant(
                event.getId(), participantUser.getId());
        p1.updateState(true, false, false);
        eventParticipantRepository.save(p1);

        // Second participation: only cash donated
        EventParticipant p2 = new EventParticipant(
                event.getId(), participantUser.getId());
        p2.updateState(false, true, false);
        eventParticipantRepository.save(p2);

        // When
        EventDetailedProjection projection = eventProjectionRepository.event(event.getId(), participantUser.getId());

        // Then
        // Should be aggregated (OR logic)
        assertThat(projection.participantState()).isNotNull();
        assertThat(projection.participantState().pollLinkSent()).isTrue(); // from p1
        assertThat(projection.participantState().cashDonated()).isTrue(); // from p2
        assertThat(projection.participantState().transferDonated()).isFalse(); // neither
    }
}
