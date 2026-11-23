package com.apparriendos.arriendos_api.controller;

import com.apparriendos.arriendos_api.model.Reserva;
import com.apparriendos.arriendos_api.security.JwtService;
import com.apparriendos.arriendos_api.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {
    private final ReservaService service;
    public ReservaController(ReservaService service) { this.service = service; }

    // Solo ADMIN (ver SecurityConfig)
    @GetMapping
    public ResponseEntity<?> getAll() { return service.getAll(); }

    /** Reservas del usuario logueado. */
    @GetMapping("/mias")
    public ResponseEntity<?> mias(@AuthenticationPrincipal Jwt jwt) {
        Long idArrendatario = arrendatarioDe(jwt);
        if (idArrendatario == null) return ResponseEntity.ok(List.of());
        return service.getByArrendatario(idArrendatario);
    }

    // Solo ADMIN
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) { return service.getById(id); }

    /**
     * ADMIN puede reservar para cualquier arrendatario. Un ARRENDATARIO siempre reserva
     * a su propio nombre, aunque envie otro idArrendatario, y la reserva queda PENDIENTE.
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Reserva reserva, @AuthenticationPrincipal Jwt jwt) {
        if (!esAdmin(jwt)) {
            Long propio = arrendatarioDe(jwt);
            if (propio == null) {
                return ResponseEntity.status(403).body(Map.of("mensaje", "Tu usuario no tiene un arrendatario asociado"));
            }
            reserva.setIdArrendatario(propio);
            reserva.setEstado("PENDIENTE");
        }
        return service.create(reserva);
    }

    // Solo ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Reserva reserva) {
        return service.update(id, reserva);
    }

    // Solo ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }

    private static boolean esAdmin(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(JwtService.CLAIM_ROLES);
        return roles != null && roles.contains("ADMIN");
    }

    private static Long arrendatarioDe(Jwt jwt) {
        Object v = jwt.getClaim(JwtService.CLAIM_ARRENDATARIO);
        return v instanceof Number n ? n.longValue() : null;
    }
}
