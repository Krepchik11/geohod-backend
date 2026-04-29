package me.geohod.geohodbackend.auth.api;

import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthCookieHelper cookieHelper;

    @PostMapping("/telegram")
    public ApiResponse<AuthTokenResponse> telegramLogin(@RequestBody TelegramLoginRequest request,
                                                        HttpServletResponse response) {
        AuthTokenResponse tokenResponse = authService.authenticate(AuthProviderType.TELEGRAM, request);
        setTokenCookiesIfAuthenticated(response, tokenResponse);
        return ApiResponse.success(tokenResponse);
    }

    @PostMapping("/telegram-oidc")
    public ApiResponse<AuthTokenResponse> telegramOidcLogin(@RequestBody TelegramOidcLoginRequest request,
                                                            HttpServletResponse response) {
        AuthTokenResponse tokenResponse = authService.authenticate(AuthProviderType.TELEGRAM, request);
        setTokenCookiesIfAuthenticated(response, tokenResponse);
        return ApiResponse.success(tokenResponse);
    }

    @PostMapping("/email/send")
    public ApiResponse<AuthTokenResponse> emailSendOtp(@RequestBody EmailOtpSendRequest request,
                                                       HttpServletResponse response) {
        AuthTokenResponse tokenResponse = authService.authenticate(AuthProviderType.EMAIL, request);
        setTokenCookiesIfAuthenticated(response, tokenResponse);
        return ApiResponse.success(tokenResponse);
    }

    @PostMapping("/email/verify")
    public ApiResponse<AuthTokenResponse> emailVerifyOtp(@RequestBody EmailOtpVerifyRequest request,
                                                         HttpServletResponse response) {
        AuthTokenResponse tokenResponse = authService.authenticate(AuthProviderType.EMAIL, request);
        setTokenCookiesIfAuthenticated(response, tokenResponse);
        return ApiResponse.success(tokenResponse);
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthTokenResponse> refresh(
            @CookieValue(name = AuthCookieHelper.REFRESH_TOKEN_COOKIE, required = false) String cookieRefreshToken,
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletResponse response) {
        String refreshToken = resolveRefreshToken(cookieRefreshToken, body);
        AuthTokenResponse tokenResponse = authService.refresh(refreshToken);
        setTokenCookiesIfAuthenticated(response, tokenResponse);
        return ApiResponse.success(tokenResponse);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = AuthCookieHelper.REFRESH_TOKEN_COOKIE, required = false) String cookieRefreshToken,
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletResponse response) {
        String refreshToken = resolveRefreshToken(cookieRefreshToken, body);
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        cookieHelper.clearTokenCookies(response);
        return ApiResponse.success(null);
    }

    @PostMapping("/link")
    public ApiResponse<Void> linkProvider(@AuthenticationPrincipal AppPrincipal principal,
                                          @RequestBody LinkProviderRequest request) {
        Object providerRequest = switch (request.providerType()) {
            case TELEGRAM -> request.oidcCode() != null
                    ? new TelegramOidcLoginRequest(request.oidcCode(), request.redirectUri(),
                            request.codeVerifier(), request.nonce(), null)
                    : new TelegramLoginRequest(request.initData());
            case EMAIL -> new EmailOtpVerifyRequest(request.email(), request.code());
        };
        authService.linkProvider(principal.userId(), request.providerType(), providerRequest);
        return ApiResponse.success(null);
    }

    private void setTokenCookiesIfAuthenticated(HttpServletResponse response, AuthTokenResponse tokenResponse) {
        if (tokenResponse.authenticated()) {
            cookieHelper.setTokenCookies(response, tokenResponse.accessToken(), tokenResponse.refreshToken());
        }
    }

    private String resolveRefreshToken(String cookieToken, RefreshTokenRequest body) {
        if (cookieToken != null) {
            return cookieToken;
        }
        return body != null ? body.refreshToken() : null;
    }
}
