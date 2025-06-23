package me.geohod.geohodbackend.configuration;

import me.geohod.geohodbackend.configuration.properties.SecurityProperties;
import me.geohod.geohodbackend.security.filter.TelegramInitDataAuthenticationFilter;
import me.geohod.geohodbackend.security.provider.TelegramTokenAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final ProviderManager providerManager;
    private final TelegramTokenAuthenticationProvider telegramTokenAuthenticationProvider;
    private final CorsFilter corsFilter;
    private final Environment environment;
    private final SecurityProperties securityProperties;

    public SecurityConfiguration(TelegramTokenAuthenticationProvider telegramTokenAuthenticationProvider,
                                CorsFilter corsFilter,
                                Environment environment,
                                SecurityProperties securityProperties) {
        this.telegramTokenAuthenticationProvider = telegramTokenAuthenticationProvider;
        this.corsFilter = corsFilter;
        this.environment = environment;
        this.securityProperties = securityProperties;
        this.providerManager = new ProviderManager(
                telegramTokenAuthenticationProvider
        );
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().fullyAuthenticated())
                .authenticationProvider(telegramTokenAuthenticationProvider)
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(loggingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tgInitDataAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(sessionConfigurer -> sessionConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Collections.singletonList("x-auth-token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private TelegramInitDataAuthenticationFilter tgInitDataAuthFilter() {
        return new TelegramInitDataAuthenticationFilter(providerManager, environment, securityProperties);
    }

    private LoggingFilter loggingFilter() {
        return new LoggingFilter();
    }
}