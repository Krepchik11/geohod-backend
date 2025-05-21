package me.geohod.geohodbackend.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.configuration.properties.SecurityProperties;
import me.geohod.geohodbackend.security.principal.TelegramPrincipal;
import me.geohod.geohodbackend.security.token.TelegramTokenAuthentication;

import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TelegramInitDataAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationManager authenticationManager;
    private final Environment environment;
    private final SecurityProperties securityProperties;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // Для отладки: проверяем, работаем ли мы в режиме разработки
        boolean isDevMode = Arrays.asList(environment.getActiveProfiles()).contains("dev") && securityProperties.isDevModeEnabled();
        
        String token = request.getHeader("Authorization");
        log.debug("Auth header: {}", token);

        if (token != null) {
            TelegramTokenAuthentication authentication = new TelegramTokenAuthentication(token);
            Authentication auth = authenticationManager.authenticate(authentication);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else if (isDevMode) {
            // В режиме разработки предоставляем тестовую аутентификацию
            log.warn("Using development authentication mode - do not use in production!");
            TelegramPrincipal devPrincipal = new TelegramPrincipal(UUID.randomUUID(), "dev_user");
            TelegramTokenAuthentication devAuth = new TelegramTokenAuthentication(devPrincipal, "dev_token");
            SecurityContextHolder.getContext().setAuthentication(devAuth);
        }

        filterChain.doFilter(request, response);
    }
}