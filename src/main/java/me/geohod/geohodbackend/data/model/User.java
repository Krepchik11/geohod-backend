package me.geohod.geohodbackend.data.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@Table("users")
public class User {
    @Version
    private Long version;
    @Id
    private UUID id;
    private String tgId;
    private String tgUsername;
    private String tgName;
    private String tgImageUrl;
    private Instant createdAt;
    private Instant updatedAt;

    public User(String tgId, String tgUsername, String tgName, String tgImageUrl) {
        Instant now = Instant.now();
        this.id = UUID.randomUUID();
        this.tgId = tgId;
        this.tgUsername = tgUsername;
        this.tgName = tgName;
        this.tgImageUrl = tgImageUrl;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void updateDetails(String username, String name, String imageUrl) {
        this.tgUsername = username;
        this.tgName = name;
        this.tgImageUrl = imageUrl;
        this.updatedAt = Instant.now();
    }
}
