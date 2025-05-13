package com.licencjat.BusinessAssistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Zezwól na żądania z Twojego frontendu
        config.addAllowedOrigin("http://localhost:4200");

        // Zezwól na wszystkie metody HTTP (GET, POST, PUT, DELETE, itp.)
        config.addAllowedMethod("*");

        // Zezwól na wszystkie nagłówki
        config.addAllowedHeader("*");

        // Zezwól na przesyłanie credentials (cookies, tokeny auth)
        config.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}