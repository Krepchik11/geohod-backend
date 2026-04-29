package me.geohod.geohodbackend.auth.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class AuthCookieHelper {

    static final String ACCESS_TOKEN_COOKIE = "access_token";
    static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/v2/auth";

    private final GeohodProperties.Cookie cookieProperties;
    private final GeohodProperties.Jwt jwtProperties;

    public AuthCookieHelper(GeohodProperties properties) {
        this.cookieProperties = properties.security().cookie();
        this.jwtProperties = properties.security().jwt();
    }

    public void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildAccessTokenCookie(accessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(refreshToken).toString());
    }

    public void clearTokenCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie(ACCESS_TOKEN_COOKIE, "/").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie(REFRESH_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE_PATH).toString());
    }

    public String extractAccessToken(HttpServletRequest request) {
        return findCookie(request, ACCESS_TOKEN_COOKIE);
    }

    public String extractRefreshToken(HttpServletRequest request) {
        return findCookie(request, REFRESH_TOKEN_COOKIE);
    }

    private ResponseCookie buildAccessTokenCookie(String value) {
        return baseCookie(ACCESS_TOKEN_COOKIE, value, "/")
                .maxAge(jwtProperties.accessTokenExpiration())
                .build();
    }

    private ResponseCookie buildRefreshTokenCookie(String value) {
        return baseCookie(REFRESH_TOKEN_COOKIE, value, REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(jwtProperties.refreshTokenExpiration())
                .build();
    }

    private ResponseCookie expiredCookie(String name, String path) {
        return baseCookie(name, "", path)
                .maxAge(0)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String name, String value, String path) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path(path);
    }

    private String findCookie(HttpServletRequest request, String name) {
        return Optional.ofNullable(request.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
