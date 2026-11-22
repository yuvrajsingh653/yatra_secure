package com.tourist.Yatra_secure.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import java.net.URI;
import java.util.List;

@Configuration
public class CorsConfig {
    @Value("${app.public-app-base-url:http://localhost:5173}")
    private String publicAppBaseUrl;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                configuredOrigin(publicAppBaseUrl)
        ).stream().distinct().toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private String configuredOrigin(String baseUrl) {
        URI uri = URI.create(baseUrl);
        if (uri.getHost() == null || uri.getUserInfo() != null
                || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
            throw new IllegalStateException("app.public-app-base-url must be an absolute HTTP(S) URL");
        }
        return uri.getScheme() + "://" + uri.getRawAuthority();
    }
}
