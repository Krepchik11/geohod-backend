package me.geohod.geohodbackend.security.provider;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.auth.data.repository.UserRoleRepository;
import me.geohod.geohodbackend.auth.telegram.TelegramInitDataVerifier;
import me.geohod.geohodbackend.auth.telegram.VerifiedInitData;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.security.principal.AppPrincipal;
import me.geohod.geohodbackend.security.token.TelegramTokenAuthentication;
import me.geohod.geohodbackend.service.IUserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramTokenAuthenticationProvider implements AuthenticationProvider {
    private final TelegramInitDataVerifier initDataVerifier;
    private final IUserService userService;
    private final UserRoleRepository userRoleRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        TelegramTokenAuthentication auth = (TelegramTokenAuthentication) authentication;
        String token = auth.getCredentials().toString();

        VerifiedInitData verifiedData = initDataVerifier.verifyAndExtract(token);

        User user = userService.createOrUpdateUser(
                verifiedData.telegramUserId(),
                verifiedData.username(),
                verifiedData.firstName(),
                verifiedData.lastName(),
                verifiedData.photoUrl()
        );

        List<String> roles = userRoleRepository.findRolesByUserId(user.getId());
        if (roles.isEmpty()) {
            roles = List.of("USER");
        }
        return new TelegramTokenAuthentication(new AppPrincipal(user.getId(), roles), token);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TelegramTokenAuthentication.class.isAssignableFrom(authentication);
    }
}
