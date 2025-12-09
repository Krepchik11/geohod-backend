package me.geohod.geohodbackend.api.controller.v2;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.request.EventRegisterRequest;
import me.geohod.geohodbackend.api.dto.request.UpdateParticipantStateRequest;
import me.geohod.geohodbackend.api.dto.response.EventParticipationCheckResponse;
import me.geohod.geohodbackend.api.dto.response.EventParticipantsResponse;
import me.geohod.geohodbackend.api.dto.response.EventRegisterResponse;
import me.geohod.geohodbackend.api.dto.response.EventRemoveParticipant;
import me.geohod.geohodbackend.api.dto.response.EventUnregisterResponse;
import me.geohod.geohodbackend.api.mapper.UserApiMapper;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.model.repository.EventParticipantProjectionRepository.EventParticipantProjection;
import me.geohod.geohodbackend.security.principal.TelegramPrincipal;
import me.geohod.geohodbackend.service.IEventParticipationService;
import me.geohod.geohodbackend.service.IEventService;
import me.geohod.geohodbackend.service.IParticipantProjectionService;

@RestController
@RequestMapping("/api/v2/events")
@RequiredArgsConstructor
public class EventParticipationController {
    private final UserApiMapper userApiMapper;
    private final IEventService eventService;
    private final IEventParticipationService participationService;
    private final IParticipantProjectionService participantProjectionService;

    @PostMapping("/{eventId}/register")
    @Operation(summary = "Register for an event", description = "Registers the authenticated user for the specified event. Optionally accepts a body with amountOfParticipants (1-3), defaults to 1.")
    public ApiResponse<EventRegisterResponse> registerForEvent(
            @PathVariable UUID eventId,
            @RequestBody(required = false) EventRegisterRequest request,
            @AuthenticationPrincipal TelegramPrincipal principal) {
        int amount = (request != null) ? request.amountOfParticipants() : 1;
        UUID loggedUserId = principal.userId();
        participationService.registerForEvent(loggedUserId, eventId, amount);
        return ApiResponse.success(new EventRegisterResponse("success"));
    }

    @DeleteMapping("/{eventId}/unregister")
    public ApiResponse<EventUnregisterResponse> unregisterFromEvent(@PathVariable UUID eventId,
            @AuthenticationPrincipal TelegramPrincipal principal) {
        UUID loggedUserId = principal.userId();
        participationService.unregisterFromEvent(loggedUserId, eventId);
        return ApiResponse.success(new EventUnregisterResponse("success"));
    }

    @DeleteMapping("/{eventId}/participants/{participantId}")
    public ApiResponse<EventRemoveParticipant> removeParticipant(@PathVariable UUID eventId,
            @PathVariable UUID participantId,
            @AuthenticationPrincipal TelegramPrincipal principal) {
        UUID loggedUserId = principal.userId();
        EventDto event = eventService.event(eventId);
        if (!event.authorId().equals(loggedUserId)) {
            throw new AccessDeniedException("You do not have permission to remove participants from this event");
        }

        participationService.unregisterParticipantFromEvent(participantId, eventId);
        return ApiResponse.success(new EventRemoveParticipant("success"));
    }

    @GetMapping("/{eventId}/participants")
    public ApiResponse<EventParticipantsResponse> eventParticipants(@PathVariable UUID eventId) {
        List<EventParticipantProjection> participants = participantProjectionService.eventParticipants(eventId);
        EventParticipantsResponse response = new EventParticipantsResponse(
                participants.stream()
                        .map(userApiMapper::map)
                        .toList());

        return ApiResponse.success(response);
    }

    @PatchMapping("/{eventId}/participation/state")
    public ApiResponse<Void> updateParticipantState(@PathVariable UUID eventId,
            @RequestBody UpdateParticipantStateRequest request,
            @AuthenticationPrincipal TelegramPrincipal principal) {
        participationService.updateParticipantState(principal.userId(), eventId, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/{eventId}/participation/check")
    @Operation(summary = "Check if user is participating in event", description = "Returns whether the currently authenticated user is registered as a participant in the specified event")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfull"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Auth missing or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Event does not exist")
    })
    public ApiResponse<EventParticipationCheckResponse> checkParticipation(
            @Parameter(required = true) @PathVariable UUID eventId,
            @AuthenticationPrincipal TelegramPrincipal principal) {
        boolean isParticipant = participationService.isUserParticipant(principal.userId(), eventId);
        return ApiResponse.success(new EventParticipationCheckResponse(isParticipant));
    }
}
