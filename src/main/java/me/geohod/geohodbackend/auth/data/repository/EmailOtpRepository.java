package me.geohod.geohodbackend.auth.data.repository;

import me.geohod.geohodbackend.auth.data.model.EmailOtp;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailOtpRepository extends CrudRepository<EmailOtp, UUID> {
    Optional<EmailOtp> findByEmailAndExpiresAtAfter(String email, Instant now);

    @Query("SELECT COUNT(*) FROM email_otp WHERE email = :email AND created_at > :since")
    long countByEmailAndCreatedAtAfter(String email, Instant since);

    @Modifying
    @Query("DELETE FROM email_otp WHERE expires_at < :now")
    void deleteExpired(Instant now);

    void deleteByEmail(String email);
}
