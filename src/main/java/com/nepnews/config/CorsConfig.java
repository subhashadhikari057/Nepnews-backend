package com.nepnews.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")  // Allow all /api/ routes
                        .allowedOrigins("http://localhost:3000","https://nepnews-frontend.vercel.app")  // Allow frontend URL

                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // allowed method
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
