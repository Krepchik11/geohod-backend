package me.geohod.geohodbackend.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Geohod Backend API")
                        .description("Backend API for Geohod event management system"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development server")
                ))
                .components(new Components()
                        .addSecuritySchemes("telegram-auth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(In.HEADER)
                                .name("Authorization")
                                .description("Telegram authentication token")))
                .addSecurityItem(new SecurityRequirement().addList("telegram-auth"));
    }
} 