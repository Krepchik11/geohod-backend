package me.geohod.geohodbackend.api.controller;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.request.EventCreateRequest;
import me.geohod.geohodbackend.api.dto.request.EventUpdateRequest;
import me.geohod.geohodbackend.api.dto.response.EventCancelResponse;
import me.geohod.geohodbackend.api.dto.response.EventCreateResponse;
import me.geohod.geohodbackend.api.dto.response.EventDetailsResponse;
import me.geohod.geohodbackend.api.dto.response.EventUpdateResponse;
import me.geohod.geohodbackend.api.mapper.EventApiMapper;
import me.geohod.geohodbackend.data.dto.CreateEventDto;
import me.geohod.geohodbackend.data.dto.EventDetailedProjection;
import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.dto.UpdateEventDto;
import me.geohod.geohodbackend.security.principal.TelegramPrincipal;
import me.geohod.geohodbackend.service.IEventProjectionService;
import me.geohod.geohodbackend.service.IEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventApiMapper mapper;
    private final IEventService eventService;
    private final IEventProjectionService eventProjectionService;

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailsResponse> getEventById(@PathVariable UUID eventId) {
        EventDetailedProjection event = eventProjectionService.event(eventId);
        return ResponseEntity.ok(mapper.response(event));
    }

    @GetMapping
    public ResponseEntity<List<EventDetailsResponse>> getAllEvents(@RequestParam(required = false, defaultValue = "true") boolean registeredOnly,
                                                                   @AuthenticationPrincipal TelegramPrincipal principal) {
        UUID filterByParticipantUserId = registeredOnly ? principal.userId() : null;
        List<EventDetailedProjection> events = eventProjectionService.events(filterByParticipantUserId);
        var result = events.stream()
                .map(mapper::response)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<EventCreateResponse> createEvent(@RequestBody EventCreateRequest request,
                                                           @AuthenticationPrincipal TelegramPrincipal principal) {
        CreateEventDto createDto = mapper.map(request, principal.userId());
        EventDto createdEvent = eventService.createEvent(createDto);

        return ResponseEntity.ok(new EventCreateResponse(createdEvent.id().toString(), "success"));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventUpdateResponse> updateEvent(@PathVariable UUID eventId,
                                                           @RequestBody EventUpdateRequest request,
                                                           @AuthenticationPrincipal TelegramPrincipal principal) {
        UUID loggedUserId = principal.userId();
        EventDto event = eventService.event(eventId);
        if (!event.authorId().equals(loggedUserId)) {
            throw new AccessDeniedException("You do not have permission to update this event");
        }

        UpdateEventDto updateDto = mapper.map(request, eventId);
        eventService.updateEventDetails(updateDto);
        return ResponseEntity.ok(new EventUpdateResponse("success"));
    }

    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<EventCancelResponse> cancelEvent(@PathVariable UUID eventId,
                                                           @AuthenticationPrincipal TelegramPrincipal principal) {
        UUID loggedUserId = principal.userId();
        EventDto event = eventService.event(eventId);
        if (!event.authorId().equals(loggedUserId)) {
            throw new AccessDeniedException("You do not have permission to cancel this event");
        }

        eventService.cancelEvent(eventId);
        return ResponseEntity.ok(new EventCancelResponse("success"));
    }
}
