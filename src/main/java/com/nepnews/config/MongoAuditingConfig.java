package com.nepnews.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class MongoAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // You can replace "system" with logged-in user ID later
        return () -> Optional.of("system");
    }
}
