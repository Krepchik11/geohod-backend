package me.geohod.geohodbackend.auth.data.repository;

import me.geohod.geohodbackend.auth.data.model.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void deleteByUserId(UUID userId);
}
