package me.geohod.geohodbackend.auth.service;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.auth.api.dto.AuthTokenResponse;
import me.geohod.geohodbackend.auth.data.model.AuthProviderEntity;
import me.geohod.geohodbackend.auth.data.model.UserRole;
import me.geohod.geohodbackend.auth.data.repository.AuthProviderRepository;
import me.geohod.geohodbackend.auth.data.repository.UserRoleRepository;
import me.geohod.geohodbackend.auth.provider.AuthProvider;
import me.geohod.geohodbackend.auth.provider.AuthProviderResult;
import me.geohod.geohodbackend.auth.provider.AuthProviderType;
import me.geohod.geohodbackend.data.model.User;
import me.geohod.geohodbackend.service.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final List<AuthProvider> providers;
    private final AuthProviderRepository authProviderRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final IUserService userService;

    @Transactional
    public AuthTokenResponse authenticate(AuthProviderType providerType, Object request) {
        AuthProvider provider = findProvider(providerType);
        AuthProviderResult result = provider.authenticate(request);

        if (!result.authenticated()) {
            return AuthTokenResponse.pending(result.message());
        }

        UUID userId = resolveOrCreateUser(result);
        return issueTokens(userId);
    }

    public AuthTokenResponse refresh(String refreshToken) {
        UUID userId = refreshTokenService.validate(refreshToken);
        refreshTokenService.revoke(refreshToken);
        return issueTokens(userId);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    private UUID resolveOrCreateUser(AuthProviderResult result) {
        return authProviderRepository.findByProviderTypeAndProviderId(result.type(), result.providerId())
                .map(AuthProviderEntity::getUserId)
                .orElseGet(() -> createNewUser(result));
    }

    private UUID createNewUser(AuthProviderResult result) {
        User user = userService.createOrUpdateUser(
                result.providerId(), result.username(), result.firstName(),
                result.lastName(), result.imageUrl());
        authProviderRepository.save(new AuthProviderEntity(user.getId(), result.type(), result.providerId()));
        userRoleRepository.save(new UserRole(user.getId(), "USER"));
        return user.getId();
    }

    private AuthTokenResponse issueTokens(UUID userId) {
        List<String> roles = userRoleRepository.findRolesByUserId(userId);
        String accessToken = jwtService.generateAccessToken(userId, roles);
        String refreshToken = refreshTokenService.create(userId);
        return AuthTokenResponse.tokens(accessToken, refreshToken);
    }

    @Transactional
    public void linkProvider(UUID userId, AuthProviderType providerType, Object request) {
        AuthProvider provider = findProvider(providerType);
        AuthProviderResult result = provider.authenticate(request);

        if (!result.authenticated()) {
            throw new SecurityException("Provider authentication failed: " + result.message());
        }

        authProviderRepository.findByProviderTypeAndProviderId(result.type(), result.providerId())
                .ifPresentOrElse(
                        existing -> { /* already linked — no-op */ },
                        () -> authProviderRepository.save(
                                new AuthProviderEntity(userId, result.type(), result.providerId()))
                );
    }

    private AuthProvider findProvider(AuthProviderType type) {
        return providers.stream()
                .filter(p -> p.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported auth provider: " + type));
    }
}
