package me.geohod.geohodbackend.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geohod.geohodbackend.configuration.properties.GeohodProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class ApplicationConfiguration implements InitializingBean {
    private final GeohodProperties properties;

    @Override
    public void afterPropertiesSet() {
        String botToken = properties.telegramBot().token();
        log.info("Bot: {}", botToken.substring(0, Math.min(3, botToken.length())));
    }
}
