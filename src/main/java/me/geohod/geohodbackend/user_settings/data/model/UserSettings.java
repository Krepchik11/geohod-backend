package me.geohod.geohodbackend.user_settings.data.model;

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
@Setter
@NoArgsConstructor
@Table("user_settings")
public class UserSettings implements Persistable<UUID> {
    @Version
    private Long version;
    @Id
    private UUID id;
    private UUID userId;
    private String defaultDonationAmount;
    private Integer defaultMaxParticipants;
    private Instant createdAt;
    private Instant updatedAt;

    public UserSettings(String defaultDonationAmount, Integer defaultMaxParticipants) {
        this.id = UUID.randomUUID();
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
    public boolean isNew() {
        return this.version == null;
    }
} 