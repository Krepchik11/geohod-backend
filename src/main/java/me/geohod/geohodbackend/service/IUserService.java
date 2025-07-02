package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.data.dto.UserDto;
import me.geohod.geohodbackend.data.model.User;

import java.util.UUID;

public interface IUserService {
    User getUser(UUID id);
    
    UserDto getUserByTelegramId(String tgId);

    User createOrUpdateUser(String tgId, String tgUsername, String firstName, String lastName, String tgImageUrl);
}
