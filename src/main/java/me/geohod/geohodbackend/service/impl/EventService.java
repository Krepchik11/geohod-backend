package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.dto.CreateEventDto;
import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.dto.FinishEventDto;
import me.geohod.geohodbackend.data.dto.UpdateEventDto;
import me.geohod.geohodbackend.data.mapper.EventModelMapper;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.data.model.repository.UserRepository;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.IEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService implements IEventService {
    private final EventModelMapper mapper;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final IEventLogService eventLogService;

    @Override
    public EventDto event(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));
        return mapper.map(event);
    }

    @Override
    public EventDto createEvent(CreateEventDto createDto) {
        if (!userRepository.existsById(createDto.authorId())) {
            throw new IllegalArgumentException("User does not exist");
        }

        Event event = new Event(
                createDto.name(),
                createDto.description(),
                createDto.date(),
                createDto.maxParticipants(),
                createDto.authorId()
        );

        eventRepository.save(event);

        EventDto result = mapper.map(event);

        String payload = String.format("{\"authorId\": \"%s\"}", result.authorId());
        eventLogService.createLogEntry(result.id(), EventType.EVENT_CREATED, payload);
        return result;
    }

    @Override
    public void updateEventDetails(UpdateEventDto updateDto) {
        Event event = eventRepository.findById(updateDto.eventId())
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));

        event.updateDetails(
                updateDto.name(),
                updateDto.description(),
                updateDto.date(),
                updateDto.maxParticipants()
        );

        eventRepository.save(event);
    }

    @Override
    @Transactional
    public void cancelEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));

        if (event.isCanceled()) {
            throw new IllegalStateException("Event already cancelled");
        }

        event.cancel();

        eventRepository.save(event);

        eventLogService.createLogEntry(eventId, EventType.EVENT_CANCELED, "{}");
    }

    @Override
    @Transactional
    public void finishEvent(FinishEventDto finishDto) {
        Event event = eventRepository.findById(finishDto.eventId())
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));

        if (event.isFinished()) {
            throw new IllegalStateException("Event already finished");
        }

        event.finish();

        eventRepository.save(event);

        String payload = String.format("{\"sendDonationRequest\": %b, \"donationInfo\": \"%s\", \"sendPollLink\": %b}",
                finishDto.sendDonationRequest(), finishDto.donationInfo(), finishDto.sendPollLink());
        eventLogService.createLogEntry(finishDto.eventId(), EventType.EVENT_FINISHED_FOR_REVIEW_LINK, payload);
    }
}
