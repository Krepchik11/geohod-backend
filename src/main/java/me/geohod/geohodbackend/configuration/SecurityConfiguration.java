package me.geohod.geohodbackend.configuration;

import me.geohod.geohodbackend.auth.service.JwtService;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import me.geohod.geohodbackend.security.filter.JwtAuthenticationFilter;
import me.geohod.geohodbackend.security.filter.TelegramInitDataAuthenticationFilter;
import me.geohod.geohodbackend.security.provider.TelegramTokenAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

    private final ProviderManager providerManager;
    private final TelegramTokenAuthenticationProvider telegramTokenAuthenticationProvider;
    private final JwtService jwtService;
    private final GeohodProperties properties;

    public SecurityConfiguration(TelegramTokenAuthenticationProvider telegramTokenAuthenticationProvider,
                                 JwtService jwtService,
                                 GeohodProperties properties) {
        this.telegramTokenAuthenticationProvider = telegramTokenAuthenticationProvider;
        this.jwtService = jwtService;
        this.properties = properties;
        this.providerManager = new ProviderManager(telegramTokenAuthenticationProvider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.cors().allowedOrigins());
        configuration.setAllowedMethods(properties.cors().allowedMethods());
        configuration.setAllowedHeaders(properties.cors().allowedHeaders());
        configuration.setAllowCredentials(properties.cors().allowCredentials());
        configuration.setMaxAge(properties.cors().maxAge());

        log.info("CORS configuration: origins={}, methods={}, headers={}, credentials={}, maxAge={}",
                properties.cors().allowedOrigins(),
                properties.cors().allowedMethods(),
                properties.cors().allowedHeaders(),
                properties.cors().allowCredentials(),
                properties.cors().maxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var chain = http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                        .requestMatchers("/api/v2/auth/**").permitAll()
                        .requestMatchers("/api/v2/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v2/**").authenticated()
                        .anyRequest().fullyAuthenticated())
                .authenticationProvider(telegramTokenAuthenticationProvider)
                .addFilterBefore(loggingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(sessionConfigurer -> sessionConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (properties.security().legacyTelegramAuth().enabled()) {
            chain.addFilterAfter(tgInitDataAuthFilter(), JwtAuthenticationFilter.class);
        }

        return chain.build();
    }

    private JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService);
    }

    private TelegramInitDataAuthenticationFilter tgInitDataAuthFilter() {
        return new TelegramInitDataAuthenticationFilter(providerManager);
    }

    private LoggingFilter loggingFilter() {
        return new LoggingFilter();
    }
}
