package me.geohod.geohodbackend.auth.api;

import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.api.response.ApiResponse;
import me.geohod.geohodbackend.auth.api.dto.AuthTokenResponse;
import me.geohod.geohodbackend.auth.api.dto.EmailOtpSendRequest;
import me.geohod.geohodbackend.auth.api.dto.EmailOtpVerifyRequest;
import me.geohod.geohodbackend.auth.api.dto.LinkProviderRequest;
import me.geohod.geohodbackend.auth.api.dto.RefreshTokenRequest;
import me.geohod.geohodbackend.auth.api.dto.TelegramLoginRequest;
import me.geohod.geohodbackend.auth.api.dto.TelegramOidcLoginRequest;
import me.geohod.geohodbackend.auth.provider.AuthProviderType;
import me.geohod.geohodbackend.auth.service.AuthService;
import me.geohod.geohodbackend.security.principal.AppPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/telegram")
    public ApiResponse<AuthTokenResponse> telegramLogin(@RequestBody TelegramLoginRequest request) {
        AuthTokenResponse response = authService.authenticate(AuthProviderType.TELEGRAM, request);
        return ApiResponse.success(response);
    }

    @PostMapping("/telegram-oidc")
    public ApiResponse<AuthTokenResponse> telegramOidcLogin(@RequestBody TelegramOidcLoginRequest request) {
        AuthTokenResponse response = authService.authenticate(AuthProviderType.TELEGRAM, request);
        return ApiResponse.success(response);
    }

    @PostMapping("/email/send")
    public ApiResponse<AuthTokenResponse> emailSendOtp(@RequestBody EmailOtpSendRequest request) {
        AuthTokenResponse response = authService.authenticate(AuthProviderType.EMAIL, request);
        return ApiResponse.success(response);
    }

    @PostMapping("/email/verify")
    public ApiResponse<AuthTokenResponse> emailVerifyOtp(@RequestBody EmailOtpVerifyRequest request) {
        AuthTokenResponse response = authService.authenticate(AuthProviderType.EMAIL, request);
        return ApiResponse.success(response);
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthTokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        AuthTokenResponse response = authService.refresh(request.refreshToken());
        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ApiResponse.success(null);
    }

    @PostMapping("/link")
    public ApiResponse<Void> linkProvider(@AuthenticationPrincipal AppPrincipal principal,
                                          @RequestBody LinkProviderRequest request) {
        Object providerRequest = switch (request.providerType()) {
            case TELEGRAM -> request.oidcCode() != null
                    ? new TelegramOidcLoginRequest(request.oidcCode(), request.redirectUri(),
                            request.codeVerifier(), request.nonce())
                    : new TelegramLoginRequest(request.initData());
            case EMAIL -> new EmailOtpVerifyRequest(request.email(), request.code());
        };
        authService.linkProvider(principal.userId(), request.providerType(), providerRequest);
        return ApiResponse.success(null);
    }
}
