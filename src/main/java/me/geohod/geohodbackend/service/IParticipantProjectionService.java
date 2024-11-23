package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.data.dto.TelegramUserDetails;

import java.util.List;
import java.util.UUID;

public interface IParticipantProjectionService {
    List<TelegramUserDetails> participantsTelegramUserProjection(UUID eventId);
}
