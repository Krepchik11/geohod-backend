package me.geohod.geohodbackend.data.model;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter;
import lombok.Setter;

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
    private String paymentGatewayUrl;
    private Boolean showBecomeOrganizer;
    private String phoneNumber;
    private Instant createdAt;
    private Instant updatedAt;

    public UserSettings() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UserSettings(UUID userId, String defaultDonationAmount, Integer defaultMaxParticipants,
            String paymentGatewayUrl, Boolean showBecomeOrganizer, String phoneNumber) {
        this.userId = userId;
        this.defaultDonationAmount = defaultDonationAmount;
        this.defaultMaxParticipants = defaultMaxParticipants;
        this.paymentGatewayUrl = paymentGatewayUrl;
        this.showBecomeOrganizer = showBecomeOrganizer;
        this.phoneNumber = phoneNumber;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UserSettings(UUID userId, String defaultDonationAmount, Integer defaultMaxParticipants) {
        this(userId, defaultDonationAmount, defaultMaxParticipants, null, null, null);
    }

    public void updateSettings(String defaultDonationAmount, Integer defaultMaxParticipants,
            String paymentGatewayUrl, Boolean showBecomeOrganizer, String phoneNumber) {
        this.defaultDonationAmount = defaultDonationAmount;
        this.defaultMaxParticipants = defaultMaxParticipants;
        this.paymentGatewayUrl = paymentGatewayUrl;
        this.showBecomeOrganizer = showBecomeOrganizer;
        this.phoneNumber = phoneNumber;
        this.updatedAt = Instant.now();
    }

    public void updateSettings(String defaultDonationAmount, Integer defaultMaxParticipants) {
        this.defaultDonationAmount = defaultDonationAmount;
        this.defaultMaxParticipants = defaultMaxParticipants;
        this.updatedAt = Instant.now();
    }

    public void updateDefaultDonationAmount(String defaultDonationAmount) {
        this.defaultDonationAmount = defaultDonationAmount;
        this.updatedAt = Instant.now();
    }

    public void updateDefaultMaxParticipants(Integer defaultMaxParticipants) {
        this.defaultMaxParticipants = defaultMaxParticipants;
        this.updatedAt = Instant.now();
    }

    public void updatePaymentGatewayUrl(String paymentGatewayUrl) {
        this.paymentGatewayUrl = paymentGatewayUrl;
        this.updatedAt = Instant.now();
    }

    public void updateShowBecomeOrganizer(Boolean showBecomeOrganizer) {
        this.showBecomeOrganizer = showBecomeOrganizer;
        this.updatedAt = Instant.now();
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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