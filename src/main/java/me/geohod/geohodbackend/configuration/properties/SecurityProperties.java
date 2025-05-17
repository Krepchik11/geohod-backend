package me.geohod.geohodbackend.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "geohod.security")
@Getter
@Setter
public class SecurityProperties {
    private boolean devModeEnabled = false;
} 