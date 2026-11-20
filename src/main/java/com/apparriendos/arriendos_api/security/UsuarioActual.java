package com.apparriendos.arriendos_api.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/** Datos del usuario logueado, sacados del JWT (sin ir a la BD). */
public record UsuarioActual(Long idUsuario, List<String> roles, Long idArrendatario) {

    public static UsuarioActual de(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(JwtService.CLAIM_ROLES);
        Object arr = jwt.getClaim(JwtService.CLAIM_ARRENDATARIO);
        return new UsuarioActual(
                Long.valueOf(jwt.getSubject()),
                roles == null ? List.of() : roles,
                arr instanceof Number n ? n.longValue() : null);
    }

    public boolean esAdmin() {
        return roles.contains("ADMIN");
    }

    /** ADMIN puede todo; un anfitrion solo lo suyo. */
    public boolean puedeEditar(Long idAnfitrion) {
        return esAdmin() || (idAnfitrion != null && idAnfitrion.equals(idUsuario));
    }
}
