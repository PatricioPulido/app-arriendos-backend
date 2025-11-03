package com.apparriendos.arriendos_api.controller;

import com.apparriendos.arriendos_api.model.Arrendatario;
import com.apparriendos.arriendos_api.service.ArrendatarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/arrendatarios")
public class ArrendatarioController {

    private final ArrendatarioService service;

    public ArrendatarioController(ArrendatarioService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Arrendatario arrendatario) {
        return service.create(arrendatario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Arrendatario arrendatario) {
        return service.update(id, arrendatario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
