package me.geohod.geohodbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan("me.geohod.geohodbackend.configuration.properties")
public class GeohodBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeohodBackendApplication.class, args);
    }

}
