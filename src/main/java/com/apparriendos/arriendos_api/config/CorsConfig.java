package com.apparriendos.arriendos_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.Duration;
import java.util.Arrays;

@Configuration
public class CorsConfig {

    // Puedes definir varios orígenes separados por coma (por ahora manual)
    // Luego podrás mover esto a BD o un servicio dinámico
    @Value("${app.cors.allowed-origins:http://localhost:4200,http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Agregar todos los orígenes permitidos
        Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .forEach(config::addAllowedOrigin);

        // Permitir todos los headers y métodos
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        // Permitir el uso de cookies / autorización (si aplica)
        config.setAllowCredentials(true);

        // Headers que el front puede leer (opcional)
        config.addExposedHeader("Location");
        config.addExposedHeader("X-Total-Count");

        // Cachear preflight OPTIONS por 1 hora
        config.setMaxAge(Duration.ofHours(1));

        // Aplicar a todos los endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
