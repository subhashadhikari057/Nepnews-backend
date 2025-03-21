package com.nepnews.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.disable()) // ✅ Disable CORS if it's causing issues
                .csrf(csrf -> csrf.disable()) // ✅ Disable CSRF for API testing
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/news/**").permitAll()  // ✅ Allow all requests to News APIs
                        .anyRequest().authenticated()  // Secure other endpoints
                )
                .formLogin(form -> form.disable()) // ✅ Disable form login (for API-only)
                .logout(logout -> logout.disable()); // ✅ Disable logout (for API-only)

        return http.build();
    }
}
