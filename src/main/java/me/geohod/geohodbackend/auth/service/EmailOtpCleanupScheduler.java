package me.geohod.geohodbackend.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.auth.data.repository.EmailOtpRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailOtpCleanupScheduler {
    private final EmailOtpRepository emailOtpRepository;

    @Scheduled(cron = "0 0 3 * * *") // daily at 3 AM
    public void cleanupExpiredOtps() {
        log.info("Cleaning up expired OTPs");
        emailOtpRepository.deleteExpired(Instant.now());
    }
}
