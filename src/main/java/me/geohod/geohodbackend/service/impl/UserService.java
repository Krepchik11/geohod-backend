package me.geohod.geohodbackend.service.impl;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.data.dto.UserDto;
import me.geohod.geohodbackend.data.mapper.UserModelMapper;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.data.model.repository.UserRepository;
import me.geohod.geohodbackend.service.IUserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {
    private final UserModelMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    public UserDto getUserByTelegramId(String tgId) {
        User user = userRepository.findByTgId(tgId)
            .orElse(null);

        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public User createOrUpdateUser(String tgId, String tgUsername, String firstName, String lastName, String tgImageUrl) {
        User existingUser = userRepository.findByTgId(tgId).orElse(null);

        if (existingUser != null) {
            if (hasUserDataChanged(existingUser, tgUsername, firstName, lastName, tgImageUrl)) {
                existingUser.updateDetails(tgUsername, firstName, lastName, tgImageUrl);
                return userRepository.save(existingUser);
            } else {
                return existingUser;
            }
        } else {
            User newUser = new User(tgId, tgUsername, firstName, lastName, tgImageUrl);
            return userRepository.save(newUser);
        }
    }

    private boolean hasUserDataChanged(User user, String newUsername, String newFirstName, String newLastName, String newImageUrl) {
        return !Objects.equals(user.getTgUsername(), newUsername) ||
               !Objects.equals(user.getFirstName(), newFirstName) ||
               !Objects.equals(user.getLastName(), newLastName) ||
               !Objects.equals(user.getTgImageUrl(), newImageUrl);
    }
}

