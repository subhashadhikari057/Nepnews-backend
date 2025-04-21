package com.nepnews.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ fix
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Auth
                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/rss/**").permitAll()


                        // News (secured first)
                        .requestMatchers(HttpMethod.GET, "/api/news/status/**").hasAnyRole("EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/news/user/**").hasAnyRole("AUTHOR", "EDITOR", "ADMIN")

                        // General GET (public news)
                        .requestMatchers(HttpMethod.GET, "/api/news/**").permitAll()

                        // Create & Update
                        .requestMatchers(HttpMethod.POST, "/api/news").hasAnyRole("AUTHOR", "EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/news/slug/**").hasAnyRole("AUTHOR", "EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/news/**").hasAnyRole("EDITOR", "ADMIN")

                        // Delete
                        .requestMatchers(HttpMethod.DELETE, "/api/news/slug/**").hasAnyRole("AUTHOR", "EDITOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/news/**").hasAnyRole("AUTHOR", "EDITOR", "ADMIN")

                        .anyRequest().authenticated()
                )


                .formLogin(login -> login.disable())
                .logout(logout -> logout.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✅ Stronger & reusable CORS config
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
