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
@Table("tg_outbox_messages")
public class TelegramOutboxMessage {
    @Version
    private Long version;
    @Id
    private Long id;
    private UUID recipientUserId;
    private String message;
    private Instant createdAt;
    private boolean processed;

    public TelegramOutboxMessage(UUID recipientUserId, String message) {
        this.recipientUserId = recipientUserId;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public void markProcessed() {
        this.processed = true;
    }
}
