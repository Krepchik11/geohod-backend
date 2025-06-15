package me.geohod.geohodbackend.data.model.repository;

import me.geohod.geohodbackend.data.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {
    Optional<User> findByTgId(String tgId);
}
