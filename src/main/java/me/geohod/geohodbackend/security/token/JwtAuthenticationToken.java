package me.geohod.geohodbackend.security.token;

import me.geohod.geohodbackend.security.principal.AppPrincipal;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final AppPrincipal principal;

    public JwtAuthenticationToken(AppPrincipal principal) {
        super(principal.getAuthorities());
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
