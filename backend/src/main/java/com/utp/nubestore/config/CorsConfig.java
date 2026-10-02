package com.utp.nubestore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración CORS: solo los orígenes declarados en
 * nubestore.cors.allowed-origins pueden consumir la API.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${nubestore.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:4200}")
    private String[] origenesPermitidos;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenesPermitidos)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}