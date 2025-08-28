package me.geohod.geohodbackend.service.impl;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
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
            // Update existing user
            existingUser.updateDetails(tgUsername, firstName, lastName, tgImageUrl);
            return userRepository.save(existingUser);
        } else {
            // Try to create new user (handle constraint violation)
            try {
                User newUser = new User(tgId, tgUsername, firstName, lastName, tgImageUrl);
                return userRepository.save(newUser);
            } catch (DataIntegrityViolationException e) {
                // Another thread created the user, fetch and return it
                log.debug("User creation race condition detected for tgId: {}, retrying...", tgId);
                return userRepository.findByTgId(tgId)
                    .orElseThrow(() -> new RuntimeException("Failed to create or find user"));
            }
        }
    }
}

