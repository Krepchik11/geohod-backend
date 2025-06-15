package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.data.model.repository.UserRepository;
import me.geohod.geohodbackend.service.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;

    @Override
    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    @Transactional
    public User createOrUpdateUser(String tgId, String tgUsername, String firstName, String lastName, String tgImageUrl) {
        return userRepository.findByTgId(tgId).orElseGet(() -> {
            User newUser = new User(tgId, tgUsername, firstName, lastName, tgImageUrl);
            return userRepository.save(newUser);
        });
    }
}