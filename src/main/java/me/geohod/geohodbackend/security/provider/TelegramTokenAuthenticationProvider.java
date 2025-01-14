package me.geohod.geohodbackend.security.provider;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.dto.TelegramInitDataDto;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.security.principal.TelegramPrincipal;
import me.geohod.geohodbackend.security.token.TelegramTokenAuthentication;
import me.geohod.geohodbackend.service.impl.TelegramTokenService;
import me.geohod.geohodbackend.service.impl.UserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramTokenAuthenticationProvider implements AuthenticationProvider {
    private final TelegramTokenService tgTokenService;
    private final UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        TelegramTokenAuthentication auth = (TelegramTokenAuthentication) authentication;
        String token = auth.getCredentials().toString();

        boolean isValid = tgTokenService.verifyTelegramWebAppData(token);

        if (isValid) {
            TelegramInitDataDto tgInitData = tgTokenService.extractUserData(token);
            User user = userService.createOrUpdateUser(
                    tgInitData.id(),
                    tgInitData.username(),
                    tgInitData.firstName(),
                    tgInitData.lastName(),
                    tgInitData.photoUrl()
            );
            return new TelegramTokenAuthentication(new TelegramPrincipal(user.getId(), user.getTgId()), token);
        }
        return auth;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TelegramTokenAuthentication.class.isAssignableFrom(authentication);
    }
}
