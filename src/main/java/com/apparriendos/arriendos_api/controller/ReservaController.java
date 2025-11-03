package com.apparriendos.arriendos_api.controller;

import com.apparriendos.arriendos_api.model.Reserva;
import com.apparriendos.arriendos_api.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {
    private final ReservaService service;
    public ReservaController(ReservaService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<?> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) { return service.getById(id); }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Reserva reserva) {
        return service.create(reserva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Reserva reserva) {
        return service.update(id, reserva);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
