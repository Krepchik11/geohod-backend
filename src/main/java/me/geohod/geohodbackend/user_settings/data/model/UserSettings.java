package me.geohod.geohodbackend.user_settings.data.model;

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
@Setter
@Table("user_settings")
public class UserSettings implements Persistable<UUID> {
    @Version
    private Long version;
    @Id
    private UUID userId;
    private String defaultDonationAmount;
    private Integer defaultMaxParticipants;
    private Instant createdAt;
    private Instant updatedAt;

    public UserSettings() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UserSettings(UUID userId, String defaultDonationAmount, Integer defaultMaxParticipants) {
        this.userId = userId;
        this.defaultDonationAmount = defaultDonationAmount;
        this.defaultMaxParticipants = defaultMaxParticipants;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateSettings(String defaultDonationAmount, Integer defaultMaxParticipants) {
        this.defaultDonationAmount = defaultDonationAmount;
        this.defaultMaxParticipants = defaultMaxParticipants;
        this.updatedAt = Instant.now();
    }

    @Override
    public UUID getId() {
        return this.userId;
    }

    @Override
    public boolean isNew() {
        return this.version == null;
    }
} 