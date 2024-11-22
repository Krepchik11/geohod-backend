package me.geohod.geohodbackend.security.token;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class TelegramTokenAuthentication extends AbstractAuthenticationToken {
    private final String token;
    private final UserDetails userDetails;

    public TelegramTokenAuthentication(String token) {
        super(null);
        this.token = token;
        this.userDetails = null;
        setAuthenticated(false);
    }

    public TelegramTokenAuthentication(UserDetails userDetails, String token) {
        super(userDetails.getAuthorities());
        this.token = token;
        this.userDetails = userDetails;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return userDetails;
    }
}
