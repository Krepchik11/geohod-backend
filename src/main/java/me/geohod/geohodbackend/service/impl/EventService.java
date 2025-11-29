package me.geohod.geohodbackend.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.dto.CancelEventDto;
import me.geohod.geohodbackend.data.dto.CreateEventDto;
import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.dto.FinishEventDto;
import me.geohod.geohodbackend.data.dto.UpdateEventDto;
import me.geohod.geohodbackend.data.mapper.EventModelMapper;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.eventlog.EventType;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.data.model.repository.UserRepository;
import me.geohod.geohodbackend.service.IEventLogService;
import me.geohod.geohodbackend.service.IEventService;

@Service
@RequiredArgsConstructor
public class EventService implements IEventService {
    private final EventModelMapper mapper;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final IEventLogService eventLogService;
    private final ObjectMapper objectMapper;

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

        Event event = mapper.map(createDto);

        eventRepository.save(event);

        EventDto result = mapper.map(event);

        String payload = toJson(java.util.Map.of("authorId", result.authorId()));
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
                updateDto.maxParticipants());

        eventRepository.save(event);
    }

    @Override
    @Transactional
    public void cancelEvent(CancelEventDto cancelDto) {
        Event event = eventRepository.findById(cancelDto.eventId())
                .orElseThrow(() -> new IllegalArgumentException("Event does not exist"));

        if (event.isCanceled()) {
            throw new IllegalStateException("Event already cancelled");
        }

        event.cancel();

        eventRepository.save(event);

        String payload = toJson(java.util.Map.of("notifyParticipants", cancelDto.notifyParticipants()));
        eventLogService.createLogEntry(cancelDto.eventId(), EventType.EVENT_CANCELED, payload);
    }

    @Override
    @Transactional
    public void finishEvent(FinishEventDto finishDto) {
        int updated = eventRepository.finishEvent(
                finishDto.eventId(),
                finishDto.sendPollLink(),
                finishDto.donationCash(),
                finishDto.donationTransfer());
        if (updated == 0) {
            throw new IllegalStateException("Event not found or already finished");
        }

        String payload = toJson(java.util.Map.of("sendPollLink", finishDto.sendPollLink()));
        eventLogService.createLogEntryAsync(finishDto.eventId(), EventType.EVENT_FINISHED_FOR_REVIEW_LINK, payload);
    }

    private String toJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event log payload", e);
        }
    }
}
