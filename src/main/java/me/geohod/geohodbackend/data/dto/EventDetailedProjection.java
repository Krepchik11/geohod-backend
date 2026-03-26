package me.geohod.geohodbackend.data.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import me.geohod.geohodbackend.data.model.Event;

public record EventDetailedProjection(
                UUID id,
                TelegramUserDetails author,
                AuthorRating authorRating,
                String name,
                String description,
                Instant date,
                int maxParticipants,
                int currentParticipants,
                Event.Status status,
                boolean sendPollLink,
                boolean donationCash,
                boolean donationTransfer,
                ParticipantState participantState) {
        public record AuthorRating(
                        BigDecimal averageRating,
                        int totalReviewsCount) {}
        
        public record ParticipantState(
                        boolean pollLinkSent,
                        boolean cashDonated,
                        boolean transferDonated) {
        }
}
