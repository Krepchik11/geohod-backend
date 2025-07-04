package me.geohod.geohodbackend.data.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@Table("users")
public class User implements Persistable<UUID> {
    @Version
    private Long version;
    @Id
    private UUID id;
    private String tgId;
    private String tgUsername;
    private String firstName;
    private String lastName;
    private String tgImageUrl;
    private Instant createdAt;
    private Instant updatedAt;

    public User(String tgId, String tgUsername, String firstName, String lastName, String tgImageUrl) {
        Instant now = Instant.now();
        this.id = UUID.randomUUID();
        this.tgId = tgId;
        this.tgUsername = tgUsername;
        this.firstName = firstName;
        this.lastName = lastName;
        this.tgImageUrl = tgImageUrl;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void updateDetails(String username, String firstName, String lastName, String imageUrl) {
        this.tgUsername = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.tgImageUrl = imageUrl;
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean isNew() {
        return this.version == null;
    }
}
