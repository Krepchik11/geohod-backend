package me.geohod.geohodbackend.auth.provider;

import me.geohod.geohodbackend.auth.api.dto.EmailOtpSendRequest;
import me.geohod.geohodbackend.auth.api.dto.EmailOtpVerifyRequest;
import me.geohod.geohodbackend.auth.data.model.EmailOtp;
import me.geohod.geohodbackend.auth.data.repository.EmailOtpRepository;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class EmailOtpAuthProvider implements AuthProvider {
    private final EmailOtpRepository emailOtpRepository;
    private final JavaMailSender mailSender;
    private final GeohodProperties.EmailOtp emailOtpProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public EmailOtpAuthProvider(GeohodProperties geohodProperties,
                                EmailOtpRepository emailOtpRepository,
                                JavaMailSender mailSender) {
        this(emailOtpRepository, mailSender, geohodProperties.security().emailOtp());
    }

    // Package-private for tests
    EmailOtpAuthProvider(EmailOtpRepository emailOtpRepository,
                         JavaMailSender mailSender,
                         GeohodProperties.EmailOtp emailOtpProperties) {
        this.emailOtpRepository = emailOtpRepository;
        this.mailSender = mailSender;
        this.emailOtpProperties = emailOtpProperties;
    }

    @Override
    public AuthProviderType getType() {
        return AuthProviderType.EMAIL;
    }

    @Override
    public AuthProviderResult authenticate(Object request) {
        if (request instanceof EmailOtpSendRequest sendRequest) {
            return handleSend(sendRequest);
        } else if (request instanceof EmailOtpVerifyRequest verifyRequest) {
            return handleVerify(verifyRequest);
        }
        throw new IllegalArgumentException("Invalid request type for Email OTP provider");
    }

    @Override
    public boolean supports(AuthProviderType type) {
        return AuthProviderType.EMAIL.equals(type);
    }

    private AuthProviderResult handleSend(EmailOtpSendRequest request) {
        String email = request.email();

        long recentCount = emailOtpRepository.countByEmailAndCreatedAtAfter(
                email, Instant.now().minusSeconds(3600));
        if (recentCount >= emailOtpProperties.maxSendsPerHour()) {
            throw new SecurityException("Too many OTP requests. Try again later.");
        }

        emailOtpRepository.deleteByEmail(email);

        String code = generateCode();
        String codeHash = hashString(code);
        Instant expiresAt = Instant.now().plus(emailOtpProperties.expiration());

        EmailOtp otp = new EmailOtp(email, codeHash, expiresAt);
        emailOtpRepository.save(otp);

        sendEmail(email, code);

        return AuthProviderResult.pending("Verification code sent to " + email);
    }

    private AuthProviderResult handleVerify(EmailOtpVerifyRequest request) {
        String email = request.email();

        EmailOtp otp = emailOtpRepository.findByEmailAndExpiresAtAfter(email, Instant.now())
                .orElseThrow(() -> new SecurityException("No valid OTP found. Request a new code."));

        if (otp.getAttempts() >= emailOtpProperties.maxAttempts()) {
            emailOtpRepository.delete(otp);
            throw new SecurityException("Maximum verification attempts exceeded. Request a new code.");
        }

        otp.incrementAttempts();

        String codeHash = hashString(request.code());
        if (!codeHash.equals(otp.getCodeHash())) {
            emailOtpRepository.save(otp);
            throw new SecurityException("Invalid verification code");
        }

        emailOtpRepository.delete(otp);
        return AuthProviderResult.authenticated(email, AuthProviderType.EMAIL);
    }

    private String generateCode() {
        int codeLength = emailOtpProperties.codeLength();
        int bound = (int) Math.pow(10, codeLength);
        int code = secureRandom.nextInt(bound);
        return String.format("%0" + codeLength + "d", code);
    }

    private void sendEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("GeoHod - Verification Code");
        message.setText("Your verification code: " + code + ". Expires in "
                + emailOtpProperties.expiration().toMinutes() + " minutes.");
        mailSender.send(message);
    }

    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
