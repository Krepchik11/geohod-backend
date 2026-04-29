package me.geohod.geohodbackend.auth.api;

import jakarta.servlet.http.Cookie;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthCookieHelperTest {

    private AuthCookieHelper cookieHelper;

    @BeforeEach
    void setUp() {
        var jwt = new GeohodProperties.Jwt("secret", Duration.ofMinutes(15), Duration.ofDays(30));
        var cookie = new GeohodProperties.Cookie("Lax", false);
        var security = new GeohodProperties.Security(jwt, null, null, null, null, cookie);
        var properties = new GeohodProperties(null, null, null, security);
        cookieHelper = new AuthCookieHelper(properties);
    }

    @Test
    void setTokenCookies_setsAccessAndRefreshCookieHeaders() {
        var response = new MockHttpServletResponse();

        cookieHelper.setTokenCookies(response, "access-token-value", "refresh-token-value");

        List<String> setCookieHeaders = response.getHeaders("Set-Cookie");
        assertEquals(2, setCookieHeaders.size());

        String accessCookie = setCookieHeaders.stream()
                .filter(h -> h.startsWith("access_token="))
                .findFirst()
                .orElseThrow();
        assertTrue(accessCookie.contains("access-token-value"));
        assertTrue(accessCookie.contains("HttpOnly"));
        assertTrue(accessCookie.contains("SameSite=Lax"));
        assertTrue(accessCookie.contains("Path=/"));

        String refreshCookie = setCookieHeaders.stream()
                .filter(h -> h.startsWith("refresh_token="))
                .findFirst()
                .orElseThrow();
        assertTrue(refreshCookie.contains("refresh-token-value"));
        assertTrue(refreshCookie.contains("HttpOnly"));
        assertTrue(refreshCookie.contains("Path=/api/v2/auth"));
    }

    @Test
    void clearTokenCookies_setsExpiredCookies() {
        var response = new MockHttpServletResponse();

        cookieHelper.clearTokenCookies(response);

        List<String> setCookieHeaders = response.getHeaders("Set-Cookie");
        assertEquals(2, setCookieHeaders.size());

        String accessCookie = setCookieHeaders.stream()
                .filter(h -> h.startsWith("access_token="))
                .findFirst()
                .orElseThrow();
        assertTrue(accessCookie.contains("Max-Age=0"));

        String refreshCookie = setCookieHeaders.stream()
                .filter(h -> h.startsWith("refresh_token="))
                .findFirst()
                .orElseThrow();
        assertTrue(refreshCookie.contains("Max-Age=0"));
    }

    @Test
    void extractAccessToken_cookiePresent_returnsValue() {
        var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("access_token", "my-jwt"));

        assertEquals("my-jwt", cookieHelper.extractAccessToken(request));
    }

    @Test
    void extractAccessToken_noCookies_returnsNull() {
        var request = new MockHttpServletRequest();

        assertNull(cookieHelper.extractAccessToken(request));
    }

    @Test
    void extractRefreshToken_cookiePresent_returnsValue() {
        var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refresh_token", "my-refresh"));

        assertEquals("my-refresh", cookieHelper.extractRefreshToken(request));
    }

    @Test
    void extractRefreshToken_cookieAbsent_returnsNull() {
        var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("other_cookie", "value"));

        assertNull(cookieHelper.extractRefreshToken(request));
    }
}
