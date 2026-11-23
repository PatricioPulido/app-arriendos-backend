package com.apparriendos.arriendos_api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuracion del login (prefijo app.jwt en application.properties).
 *
 * @param secret     clave HMAC para firmar los tokens; minimo 32 caracteres (JWT_SECRET)
 * @param issuer     emisor que se escribe y se exige en cada token
 * @param accessTtl  duracion del token de acceso (corto: va en cada request)
 * @param refreshTtl duracion del token de refresco (cookie HttpOnly)
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, String issuer, Duration accessTtl, Duration refreshTtl) {

    public JwtProperties {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "Define JWT_SECRET con al menos 32 caracteres (por ejemplo: openssl rand -base64 48)");
        }
        if (issuer == null || issuer.isBlank()) issuer = "arriendos-api";
        if (accessTtl == null) accessTtl = Duration.ofMinutes(15);
        if (refreshTtl == null) refreshTtl = Duration.ofDays(7);
    }
}
