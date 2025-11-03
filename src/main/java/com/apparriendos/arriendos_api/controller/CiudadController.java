package com.apparriendos.arriendos_api.controller;

import com.apparriendos.arriendos_api.model.Ciudad;
import com.apparriendos.arriendos_api.service.CiudadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ciudades")
public class CiudadController {
    private final CiudadService service;
    public CiudadController(CiudadService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<?> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) { return service.getById(id); }

    // Admin endpoints (protege con seguridad cuando implementes auth)
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Ciudad ciudad) { return service.create(ciudad); }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Ciudad ciudad) { return service.update(id, ciudad); }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) { return service.delete(id); }
}
