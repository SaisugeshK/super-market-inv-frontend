package com.example.InventoryManagementSystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * A WebMvcConfigurer#addCorsMappings registration (the previous approach here)
 * only applies inside DispatcherServlet — AFTER Spring Security's
 * FilterChainProxy has already let a request through. Any request Security
 * itself rejects (401 from authenticationEntryPoint, 403 from
 * accessDeniedHandler — e.g. a non-admin hitting an admin-only endpoint)
 * never reaches the dispatcher, so it never got a CORS header, and a real
 * browser reports that as "blocked by CORS policy" instead of showing the
 * actual 401/403. Exposing a CorsConfigurationSource bean instead lets
 * SecurityConfig wire CORS into the security filter chain itself (see
 * `.cors(...)` there), so every response — allowed or rejected — carries the
 * right headers.
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String[] allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String[] allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String[] allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
