package me.geohod.geohodbackend.auth.data.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Table("email_otp")
public class EmailOtp implements Persistable<UUID> {
    @Version
    private Long version;
    @Id
    private UUID id;
    private String email;
    private String codeHash;
    private Instant expiresAt;
    private int attempts;
    private Instant createdAt;

    public EmailOtp(String email, String codeHash, Instant expiresAt) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.attempts = 0;
        this.createdAt = Instant.now();
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    @Override
    public boolean isNew() {
        return this.version == null;
    }
}
