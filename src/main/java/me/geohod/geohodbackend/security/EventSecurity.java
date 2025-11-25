package me.geohod.geohodbackend.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.Event;
import me.geohod.geohodbackend.data.model.repository.EventRepository;
import me.geohod.geohodbackend.security.principal.TelegramPrincipal;

@Service
@RequiredArgsConstructor
public class EventSecurity {

    private final EventRepository eventRepository;

    public boolean isEventAuthor(UUID eventId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (eventId == null || authentication == null
                || !(authentication.getPrincipal() instanceof TelegramPrincipal principal)) {
            return false;
        }

        return eventRepository.findById(eventId)
                .map(Event::getAuthorId)
                .map(authorId -> authorId.equals(principal.userId()))
                .orElse(false);
    }
}
