package me.geohod.geohodbackend.auth.data.repository;

import me.geohod.geohodbackend.auth.data.model.AuthProviderEntity;
import me.geohod.geohodbackend.auth.provider.AuthProviderType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthProviderRepository extends CrudRepository<AuthProviderEntity, UUID> {
    Optional<AuthProviderEntity> findByProviderTypeAndProviderId(AuthProviderType providerType, String providerId);
    Optional<AuthProviderEntity> findByUserIdAndProviderType(UUID userId, AuthProviderType providerType);
}
