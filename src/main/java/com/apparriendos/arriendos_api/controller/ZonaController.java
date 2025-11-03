package com.apparriendos.arriendos_api.controller;

import com.apparriendos.arriendos_api.model.Zona;
import com.apparriendos.arriendos_api.service.ZonaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/zonas")
public class ZonaController {
    private final ZonaService service;
    public ZonaController(ZonaService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(value = "ciudadId", required = false) Long ciudadId) {
        if (ciudadId == null) return service.getAll();
        return service.getByCiudad(ciudadId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) { return service.getById(id); }

    // Admin
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Zona zona) { return service.create(zona); }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Zona zona) { return service.update(id, zona); }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) { return service.delete(id); }
}
