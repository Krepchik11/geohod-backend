package me.geohod.geohodbackend.api.controller.v2;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.request.EventCancelRequest;
import me.geohod.geohodbackend.api.dto.request.EventCreateRequest;
import me.geohod.geohodbackend.api.dto.request.EventFinishRequest;
import me.geohod.geohodbackend.api.dto.request.EventUpdateRequest;
import me.geohod.geohodbackend.api.dto.response.EventCancelResponse;
import me.geohod.geohodbackend.api.dto.response.EventCreateResponse;
import me.geohod.geohodbackend.api.dto.response.EventDetailsResponse;
import me.geohod.geohodbackend.api.dto.response.EventFinishResponse;
import me.geohod.geohodbackend.api.dto.response.EventUpdateResponse;
import me.geohod.geohodbackend.api.mapper.EventApiMapper;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.api.response.PageResponse;
import me.geohod.geohodbackend.data.dto.CancelEventDto;
import me.geohod.geohodbackend.data.dto.CreateEventDto;
import me.geohod.geohodbackend.data.dto.EventDetailedProjection;
import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.dto.UpdateEventDto;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.security.principal.TelegramPrincipal;
import me.geohod.geohodbackend.service.IEventProjectionService;
import me.geohod.geohodbackend.service.IEventService;

@RestController(value = "EventControllerV2")
@RequestMapping("/api/v2/events")
@RequiredArgsConstructor
public class EventController {
    private final EventApiMapper mapper;
    private final IEventService eventService;
    private final IEventProjectionService eventProjectionService;

    @GetMapping("/{eventId}")
    public ApiResponse<EventDetailsResponse> getEventById(@PathVariable UUID eventId) {
        EventDetailedProjection event = eventProjectionService.event(eventId);
        return ApiResponse.success(mapper.response(event));
    }

    @GetMapping
    public ApiResponse<PageResponse<EventDetailsResponse>> getAllEvents(@RequestParam(required = false, defaultValue = "true") boolean iamAuthor,
                                                                   @RequestParam(required = false, defaultValue = "true") boolean iamParticipant,
                                                                   @RequestParam(required = false) List<Event.Status> statuses,
                                                                   @PageableDefault(size = 30) Pageable pageable,
                                                                   @AuthenticationPrincipal TelegramPrincipal principal) {
        UUID filterByAuthorUserId = iamAuthor ? principal.userId() : null;
        UUID filterByParticipantUserId = iamParticipant ? principal.userId() : null;
        Page<EventDetailedProjection> events = eventProjectionService.events(
                new IEventProjectionService.EventsDetailedProjectionFilter(filterByAuthorUserId, filterByParticipantUserId, statuses),
                pageable
        );
        Page<EventDetailsResponse> result = events.map(mapper::response);
        return ApiResponse.success(new PageResponse<>(result));
    }

    @PostMapping
    public ApiResponse<EventCreateResponse> createEvent(@RequestBody EventCreateRequest request,
                                                           @AuthenticationPrincipal TelegramPrincipal principal) {
        CreateEventDto createDto = mapper.map(request, principal.userId());
        EventDto createdEvent = eventService.createEvent(createDto);

        EventCreateResponse response = mapper.response(createdEvent);

        return ApiResponse.success(response);
    }

    @PutMapping("/{eventId}")
    public ApiResponse<EventUpdateResponse> updateEvent(@PathVariable UUID eventId,
                                                           @RequestBody EventUpdateRequest request,
                                                           @AuthenticationPrincipal TelegramPrincipal principal) {
        UUID loggedUserId = principal.userId();
        EventDto event = eventService.event(eventId);
        if (!event.authorId().equals(loggedUserId)) {
            throw new AccessDeniedException("You do not have permission to update this event");
        }

        UpdateEventDto updateDto = mapper.map(request, eventId);
        eventService.updateEventDetails(updateDto);
        return ApiResponse.success(new EventUpdateResponse("success"));
    }

    @PatchMapping("/{eventId}/cancel")
    public ApiResponse<EventCancelResponse> cancelEvent(@PathVariable UUID eventId,
                                                           @RequestBody EventCancelRequest request,
                                                           @AuthenticationPrincipal TelegramPrincipal principal) {
        UUID loggedUserId = principal.userId();
        EventDto event = eventService.event(eventId);
        if (!event.authorId().equals(loggedUserId)) {
            throw new AccessDeniedException("You do not have permission to cancel this event");
        }

        eventService.cancelEvent(new CancelEventDto(eventId, request.notifyParticipants()));
        return ApiResponse.success(new EventCancelResponse("success"));
    }

    @PatchMapping("/{eventId}/finish")
    public ApiResponse<EventFinishResponse> finishEvent(@PathVariable UUID eventId,
                                                           @RequestBody EventFinishRequest request,
                                                           @AuthenticationPrincipal TelegramPrincipal principal) {
        UUID loggedUserId = principal.userId();
        EventDto event = eventService.event(eventId);
        if (!event.authorId().equals(loggedUserId)) {
            throw new AccessDeniedException("You do not have permission to finish this event");
        }

        eventService.finishEvent(mapper.map(request, eventId));
        return ApiResponse.success(new EventFinishResponse("success"));
    }
} 