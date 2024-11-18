package me.geohod.geohodbackend.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@Table("users")
public class User {
    @Id
    private UUID id;
    private String username;
    private String name;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;

    public User(UUID id, String username, String name, String imageUrl) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.imageUrl = imageUrl;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateDetails(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.updatedAt = Instant.now();
    }
}
