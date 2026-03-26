package me.geohod.geohodbackend.auth.provider;

import me.geohod.geohodbackend.auth.api.dto.EmailOtpSendRequest;
import me.geohod.geohodbackend.auth.api.dto.EmailOtpVerifyRequest;
import me.geohod.geohodbackend.auth.data.model.EmailOtp;
import me.geohod.geohodbackend.auth.data.repository.EmailOtpRepository;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailOtpAuthProviderTest {
    @Mock EmailOtpRepository emailOtpRepository;
    @Mock JavaMailSender mailSender;
    private EmailOtpAuthProvider provider;

    @BeforeEach
    void setUp() {
        var emailOtpProps = new GeohodProperties.EmailOtp(6, Duration.ofMinutes(5), 5, 3);
        provider = new EmailOtpAuthProvider(emailOtpRepository, mailSender, emailOtpProps);
    }

    @Test
    void authenticate_sendRequest_sendsEmailAndReturnsPending() {
        String email = "test@example.com";
        when(emailOtpRepository.countByEmailAndCreatedAtAfter(eq(email), any())).thenReturn(0L);

        AuthProviderResult result = provider.authenticate(new EmailOtpSendRequest(email));

        assertFalse(result.authenticated());
        verify(emailOtpRepository).deleteByEmail(email);
        verify(emailOtpRepository).save(any(EmailOtp.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void authenticate_sendRequest_rateLimitExceeded_throws() {
        String email = "test@example.com";
        when(emailOtpRepository.countByEmailAndCreatedAtAfter(eq(email), any())).thenReturn(3L);

        assertThrows(SecurityException.class,
                () -> provider.authenticate(new EmailOtpSendRequest(email)));
    }

    @Test
    void authenticate_verifyRequest_wrongCode_incrementsAttemptsAndThrows() {
        String email = "test@example.com";
        EmailOtp otp = mock(EmailOtp.class);
        when(otp.getAttempts()).thenReturn(0);
        when(otp.getCodeHash()).thenReturn("wrong-hash");

        when(emailOtpRepository.findByEmailAndExpiresAtAfter(eq(email), any()))
                .thenReturn(Optional.of(otp));

        assertThrows(SecurityException.class,
                () -> provider.authenticate(new EmailOtpVerifyRequest(email, "123456")));
        verify(otp).incrementAttempts();
    }

    @Test
    void authenticate_verifyRequest_maxAttemptsExceeded_throws() {
        String email = "test@example.com";
        EmailOtp otp = mock(EmailOtp.class);
        when(otp.getAttempts()).thenReturn(5);

        when(emailOtpRepository.findByEmailAndExpiresAtAfter(eq(email), any()))
                .thenReturn(Optional.of(otp));

        assertThrows(SecurityException.class,
                () -> provider.authenticate(new EmailOtpVerifyRequest(email, "123456")));
    }

    @Test
    void authenticate_verifyRequest_noOtp_throws() {
        when(emailOtpRepository.findByEmailAndExpiresAtAfter(eq("test@example.com"), any()))
                .thenReturn(Optional.empty());

        assertThrows(SecurityException.class,
                () -> provider.authenticate(new EmailOtpVerifyRequest("test@example.com", "123456")));
    }

    @Test
    void supports_email_returnsTrue() {
        assertTrue(provider.supports(AuthProviderType.EMAIL));
    }
}
