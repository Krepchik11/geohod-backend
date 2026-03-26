package me.geohod.geohodbackend.security.provider;

import me.geohod.geohodbackend.auth.data.repository.UserRoleRepository;
import me.geohod.geohodbackend.auth.telegram.TelegramInitDataVerifier;
import me.geohod.geohodbackend.auth.telegram.VerifiedInitData;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.security.principal.AppPrincipal;
import me.geohod.geohodbackend.security.token.TelegramTokenAuthentication;
import me.geohod.geohodbackend.service.IUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramTokenAuthenticationProviderTest {

    @Mock TelegramInitDataVerifier initDataVerifier;
    @Mock IUserService userService;
    @Mock UserRoleRepository userRoleRepository;
    @InjectMocks TelegramTokenAuthenticationProvider provider;

    @Test
    void authenticate_validToken_returnsAuthenticatedToken() {
        String token = "valid-init-data";
        var auth = new TelegramTokenAuthentication(token);
        UUID userId = UUID.randomUUID();
        var user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        when(initDataVerifier.verifyAndExtract(token))
                .thenReturn(new VerifiedInitData("12345", "testuser", "John", "Doe", null));
        when(userService.createOrUpdateUser("12345", "testuser", "John", "Doe", null))
                .thenReturn(user);
        when(userRoleRepository.findRolesByUserId(userId)).thenReturn(List.of("USER"));

        var result = provider.authenticate(auth);

        assertTrue(result.isAuthenticated());
        assertInstanceOf(AppPrincipal.class, result.getPrincipal());
        var principal = (AppPrincipal) result.getPrincipal();
        assertEquals(userId, principal.userId());
        assertEquals(List.of("USER"), principal.roles());
    }

    @Test
    void authenticate_invalidToken_throwsSecurityException() {
        String token = "invalid-data";
        var auth = new TelegramTokenAuthentication(token);

        when(initDataVerifier.verifyAndExtract(token))
                .thenThrow(new SecurityException("Invalid initData hash"));

        assertThrows(SecurityException.class, () -> provider.authenticate(auth));
    }

    @Test
    void authenticate_emptyRoles_defaultsToUser() {
        String token = "valid-init-data";
        var auth = new TelegramTokenAuthentication(token);
        UUID userId = UUID.randomUUID();
        var user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        when(initDataVerifier.verifyAndExtract(token))
                .thenReturn(new VerifiedInitData("12345", "testuser", "John", "Doe", null));
        when(userService.createOrUpdateUser("12345", "testuser", "John", "Doe", null))
                .thenReturn(user);
        when(userRoleRepository.findRolesByUserId(userId)).thenReturn(List.of());

        var result = provider.authenticate(auth);

        var principal = (AppPrincipal) result.getPrincipal();
        assertEquals(List.of("USER"), principal.roles());
    }

    @Test
    void supports_telegramTokenAuthentication_returnsTrue() {
        assertTrue(provider.supports(TelegramTokenAuthentication.class));
    }
}
