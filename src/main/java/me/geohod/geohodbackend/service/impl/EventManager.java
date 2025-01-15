package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.service.IEventManager;
import me.geohod.geohodbackend.service.IEventNotificationService;
import me.geohod.geohodbackend.service.IEventService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventManager implements IEventManager {
    private final IEventService eventService;
    private final IEventNotificationService eventNotificationService;

    @Override
    public void cancelEvent(UUID eventId) {
        eventService.cancelEvent(eventId);
        eventNotificationService.notifyParticipantsEventCancelled(eventId);
    }
}
