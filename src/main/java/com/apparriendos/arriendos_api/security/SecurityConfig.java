package com.apparriendos.arriendos_api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.util.Map;

/**
 * Reglas de acceso:
 * - Publico: catalogo (GET de departamentos, ciudades y zonas), login, registro, refresh.
 * - ARRENDATARIO: crear reservas a su nombre y ver las suyas.
 * - ADMIN: todo lo demas.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] CATALOGO = {"/api/departamentos/**", "/api/ciudades/**", "/api/zonas/**"};

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API sin sesion: el token viaja en el header Authorization, no en una cookie de sesion
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // usa el bean corsFilter de CorsConfig
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/login", "/api/auth/register", "/api/auth/refresh", "/api/auth/logout").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, CATALOGO).permitAll()
                        .requestMatchers(CATALOGO).hasRole("ADMIN")
                        .requestMatchers("/api/arrendatarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/reservas").hasAnyRole("ADMIN", "ARRENDATARIO")
                        .requestMatchers(HttpMethod.GET, "/api/reservas/mias").hasAnyRole("ADMIN", "ARRENDATARIO")
                        .requestMatchers("/api/reservas/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(rs -> rs
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint((req, res, ex) -> writeJson(res, 401, "No autenticado o token inválido"))
                        .accessDeniedHandler((req, res, ex) -> writeJson(res, 403, "No tienes permiso para esta acción")))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> writeJson(res, 401, "No autenticado o token inválido"))
                        .accessDeniedHandler((req, res, ex) -> writeJson(res, 403, "No tienes permiso para esta acción")));
        return http.build();
    }

    /** Convierte el claim "roles" del JWT en autoridades ROLE_ADMIN / ROLE_ARRENDATARIO. */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(JwtService.CLAIM_ROLES);
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    private void writeJson(HttpServletResponse res, int status, String mensaje) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(res.getWriter(), Map.of("mensaje", mensaje));
    }
}
