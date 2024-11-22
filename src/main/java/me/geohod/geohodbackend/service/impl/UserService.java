package me.geohod.geohodbackend.service.impl;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.data.model.repository.UserRepository;
import me.geohod.geohodbackend.service.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public User createOrUpdateUser(String tgId, String tgUsername, String tgName, String tgImageUrl) {
        return userRepository.findByTgId(tgId).orElseGet(() -> {
            User newUser = new User(tgId, tgUsername, tgName, tgImageUrl);
            return userRepository.save(newUser);
        });
    }
}