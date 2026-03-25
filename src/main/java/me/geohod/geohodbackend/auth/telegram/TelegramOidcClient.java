package me.geohod.geohodbackend.auth.telegram;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;

@Component
@RequiredArgsConstructor
public class TelegramOidcClient {

    private final GeohodProperties properties;
    private final RestClient.Builder restClientBuilder;

    private RestClient restClient;
    private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    @PostConstruct
    void init() {
        restClient = restClientBuilder.build();

        var oidcConfig = properties.security().telegramOidc();

        try {
            long cacheTtlMs = oidcConfig.jwksCacheTtl().toMillis();
            long refreshTimeoutMs = Math.max(cacheTtlMs / 2, 1000);
            JWKSource<SecurityContext> jwkSource = JWKSourceBuilder
                    .create(URI.create(oidcConfig.jwksUri()).toURL())
                    .cache(cacheTtlMs, refreshTimeoutMs)
                    .build();

            var keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);

            var expectedClaims = new JWTClaimsSet.Builder()
                    .issuer(oidcConfig.issuerUri())
                    .audience(oidcConfig.clientId())
                    .build();

            Set<String> requiredClaims = new HashSet<>(Set.of("sub", "iat", "exp"));

            var claimsVerifier = new DefaultJWTClaimsVerifier<SecurityContext>(expectedClaims, requiredClaims);

            jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(keySelector);
            jwtProcessor.setJWTClaimsSetVerifier(claimsVerifier);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid JWKS URI: " + oidcConfig.jwksUri(), e);
        }
    }

    public OidcUserInfo exchangeAndVerify(String code, String redirectUri,
                                          @Nullable String codeVerifier,
                                          @Nullable String nonce) {
        String idTokenString = exchangeCodeForIdToken(code, redirectUri, codeVerifier);
        JWTClaimsSet claims = verifyIdToken(idTokenString);
        validateNonce(claims, nonce);
        return extractUserInfo(claims);
    }

    @SuppressWarnings("unchecked")
    private String exchangeCodeForIdToken(String code, String redirectUri, @Nullable String codeVerifier) {
        var oidcConfig = properties.security().telegramOidc();

        String credentials = oidcConfig.clientId() + ":" + oidcConfig.clientSecret();
        String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        var formData = new LinkedMultiValueMap<String, String>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);
        if (codeVerifier != null) {
            formData.add("code_verifier", codeVerifier);
        }

        Map<String, Object> response = restClient.post()
                .uri(oidcConfig.tokenUri())
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(formData)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("id_token")) {
            throw new SecurityException("Token response does not contain id_token");
        }

        return (String) response.get("id_token");
    }

    private JWTClaimsSet verifyIdToken(String idTokenString) {
        try {
            return jwtProcessor.process(idTokenString, null);
        } catch (Exception e) {
            throw new SecurityException("ID token verification failed: " + e.getMessage(), e);
        }
    }

    void validateNonce(JWTClaimsSet claims, @Nullable String expectedNonce) {
        if (expectedNonce == null) {
            return;
        }

        Object nonceClaim = claims.getClaim("nonce");
        if (nonceClaim == null || !expectedNonce.equals(nonceClaim.toString())) {
            throw new SecurityException("Nonce mismatch in ID token");
        }
    }

    OidcUserInfo extractUserInfo(JWTClaimsSet claims) {
        String sub = claims.getSubject();
        String name = (String) claims.getClaim("name");
        String username = (String) claims.getClaim("preferred_username");
        String picture = (String) claims.getClaim("picture");

        return new OidcUserInfo(sub, name, username, picture);
    }
}
