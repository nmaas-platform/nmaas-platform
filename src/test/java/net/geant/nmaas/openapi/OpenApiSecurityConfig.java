package net.geant.nmaas.openapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("openapi")
public class OpenApiSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html") // Restrict to OpenAPI endpoints
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll() // Allow unauthenticated access to matched endpoints
                )
                .csrf(csrf -> csrf.disable()); // Disable CSRF for test environment
        return http.build();
    }
}