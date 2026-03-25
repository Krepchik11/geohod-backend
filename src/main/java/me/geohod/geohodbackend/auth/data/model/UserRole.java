package me.geohod.geohodbackend.auth.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@Getter
@NoArgsConstructor
@Table("user_role")
public class UserRole implements Persistable<UUID> {
    @Id
    private UUID id;
    private UUID userId;
    private String role;

    public UserRole(UUID userId, String role) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.role = role;
    }

    @Override
    public boolean isNew() {
        return true; // user roles are always new (never updated, only created/deleted)
    }
}
