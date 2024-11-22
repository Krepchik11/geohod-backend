package me.geohod.geohodbackend.service;

import me.geohod.geohodbackend.data.model.User;

public interface IUserService {
    User createOrUpdateUser(String tgId, String tgUsername, String tgName, String tgImageUrl);
}
