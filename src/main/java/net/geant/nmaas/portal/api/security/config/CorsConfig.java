package net.geant.nmaas.portal.api.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Configuration class responsible for setting up Cross-Origin Resource Sharing (CORS) settings
 * for the application's REST API endpoints.
 * This configuration allows the frontend (hosted under the specified origin) to make requests
 * to backend endpoints under the "/api/**" path, including support for sending credentials (e.g., cookies, Authorization headers).
 */
@Configuration
public class CorsConfig {

    /**
     * The allowed origin (e.g., frontend address) loaded from application properties.
     * Example for local applications: http://localhost:4200
     */
    @Value("${portal.address}")
    private String allowedOrigin;

    /**
     * Defines the CORS filter bean used by Spring Security to handle CORS preflight and actual requests.
     *
     * @return a configured {@link CorsFilter} instance that applies CORS rules to API endpoints.
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(allowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}

