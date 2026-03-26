package me.geohod.geohodbackend.auth.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.geohod.geohodbackend.auth.provider.AuthProviderType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Table("auth_provider")
public class AuthProviderEntity implements Persistable<UUID> {
    @Version
    private Long version;
    @Id
    private UUID id;
    private UUID userId;
    private AuthProviderType providerType;
    private String providerId;
    private Instant createdAt;
    private Instant lastModifiedDate;

    public AuthProviderEntity(UUID userId, AuthProviderType providerType, String providerId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.providerType = providerType;
        this.providerId = providerId;
        this.createdAt = Instant.now();
        this.lastModifiedDate = Instant.now();
    }

    @Override
    public boolean isNew() {
        return this.version == null;
    }
}
