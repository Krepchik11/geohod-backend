package me.geohod.geohodbackend.auth.provider;

import me.geohod.geohodbackend.auth.api.dto.TelegramLoginRequest;
import me.geohod.geohodbackend.auth.api.dto.TelegramOidcLoginRequest;
import me.geohod.geohodbackend.auth.telegram.OidcUserInfo;
import me.geohod.geohodbackend.auth.telegram.TelegramInitDataVerifier;
import me.geohod.geohodbackend.auth.telegram.TelegramOidcClient;
import me.geohod.geohodbackend.auth.telegram.VerifiedInitData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramAuthProviderTest {
    @Mock TelegramInitDataVerifier telegramInitDataVerifier;
    @Mock TelegramOidcClient telegramOidcClient;
    @InjectMocks TelegramAuthProvider telegramAuthProvider;

    @Test
    void authenticate_validInitData_returnsAuthenticatedResult() {
        String initData = "valid-init-data";
        when(telegramInitDataVerifier.verifyAndExtract(initData))
                .thenReturn(new VerifiedInitData("12345", "johndoe", "John", "Doe", null));

        AuthProviderResult result = telegramAuthProvider.authenticate(new TelegramLoginRequest(initData));

        assertTrue(result.authenticated());
        assertEquals("12345", result.providerId());
        assertEquals(AuthProviderType.TELEGRAM, result.type());
        assertEquals("johndoe", result.username());
        assertEquals("John", result.firstName());
    }

    @Test
    void authenticate_invalidInitData_throwsSecurityException() {
        String initData = "invalid-init-data";
        when(telegramInitDataVerifier.verifyAndExtract(initData))
                .thenThrow(new SecurityException("Invalid initData hash"));

        assertThrows(SecurityException.class,
                () -> telegramAuthProvider.authenticate(new TelegramLoginRequest(initData)));
    }

    @Test
    void supports_telegram_returnsTrue() {
        assertTrue(telegramAuthProvider.supports(AuthProviderType.TELEGRAM));
    }

    @Test
    void supports_email_returnsFalse() {
        assertFalse(telegramAuthProvider.supports(AuthProviderType.EMAIL));
    }

    @Test
    void authenticate_withOidcRequest_delegatesToOidcClient() {
        var request = new TelegramOidcLoginRequest("auth-code", "https://redirect.url", "verifier", "nonce");
        var oidcUserInfo = new OidcUserInfo("99999", "Jane Doe", "janedoe", "https://photo.url");
        when(telegramOidcClient.exchangeAndVerify("auth-code", "https://redirect.url", "verifier", "nonce"))
                .thenReturn(oidcUserInfo);

        AuthProviderResult result = telegramAuthProvider.authenticate(request);

        assertEquals("99999", result.providerId());
        assertEquals(AuthProviderType.TELEGRAM, result.type());
        assertTrue(result.authenticated());
        assertEquals("janedoe", result.username());
        assertEquals("Jane Doe", result.firstName());
        verify(telegramOidcClient).exchangeAndVerify("auth-code", "https://redirect.url", "verifier", "nonce");
        verifyNoInteractions(telegramInitDataVerifier);
    }

    @Test
    void authenticate_unsupportedRequestType_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> telegramAuthProvider.authenticate("invalid"));
    }
}
