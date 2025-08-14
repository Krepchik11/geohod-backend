package me.geohod.geohodbackend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import me.geohod.geohodbackend.security.filter.TelegramInitDataAuthenticationFilter;
import me.geohod.geohodbackend.security.provider.TelegramTokenAuthenticationProvider;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final ProviderManager providerManager;
    private final TelegramTokenAuthenticationProvider telegramTokenAuthenticationProvider;

    public SecurityConfiguration(TelegramTokenAuthenticationProvider telegramTokenAuthenticationProvider) {
        this.telegramTokenAuthenticationProvider = telegramTokenAuthenticationProvider;
        this.providerManager = new ProviderManager(
                telegramTokenAuthenticationProvider);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                    .anyRequest().fullyAuthenticated())
                .authenticationProvider(telegramTokenAuthenticationProvider)
                .addFilterBefore(loggingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tgInitDataAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(sessionConfigurer -> sessionConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    private TelegramInitDataAuthenticationFilter tgInitDataAuthFilter() {
        return new TelegramInitDataAuthenticationFilter(providerManager);
    }

    private LoggingFilter loggingFilter() {
        return new LoggingFilter();
    }
}