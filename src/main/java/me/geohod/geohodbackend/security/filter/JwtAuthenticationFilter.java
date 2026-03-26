package me.geohod.geohodbackend.security.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.auth.service.JwtService;
import me.geohod.geohodbackend.security.principal.AppPrincipal;
import me.geohod.geohodbackend.security.token.JwtAuthenticationToken;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            try {
                Claims claims = jwtService.validateAccessToken(token);
                UUID userId = UUID.fromString(claims.getSubject());
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                AppPrincipal principal = new AppPrincipal(userId, roles);
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(principal);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.debug("JWT validation failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
