package me.geohod.geohodbackend.api.controller.v2;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.response.EventParticipantsResponse;
import me.geohod.geohodbackend.api.dto.response.EventRegisterResponse;
import me.geohod.geohodbackend.api.dto.response.EventRemoveParticipant;
import me.geohod.geohodbackend.api.dto.response.EventUnregisterResponse;
import me.geohod.geohodbackend.api.mapper.UserApiMapper;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.data.dto.EventDto;
import me.geohod.geohodbackend.data.dto.EventParticipantProjection;
import me.geohod.geohodbackend.security.principal.TelegramPrincipal;
import me.geohod.geohodbackend.service.IEventParticipationService;
import me.geohod.geohodbackend.service.IEventService;
import me.geohod.geohodbackend.service.IParticipantProjectionService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/events")
@RequiredArgsConstructor
public class EventParticipationController {
    private final UserApiMapper userApiMapper;
    private final IEventService eventService;
    private final IEventParticipationService participationService;
    private final IParticipantProjectionService participantProjectionService;

    @PostMapping("/{eventId}/register")
    public ApiResponse<EventRegisterResponse> registerForEvent(@PathVariable UUID eventId,
                                                                  @AuthenticationPrincipal TelegramPrincipal principal) {
        UUID loggedUserId = principal.userId();
        participationService.registerForEvent(loggedUserId, eventId);
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
                        .toList()
        );

        return ApiResponse.success(response);
    }
} 