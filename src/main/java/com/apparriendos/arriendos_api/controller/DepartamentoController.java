package com.apparriendos.arriendos_api.controller;

import com.apparriendos.arriendos_api.model.Departamento;
import com.apparriendos.arriendos_api.service.DepartamentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departamentos")
//@CrossOrigin(origins = "*") // No olvides esto para el frontend
public class DepartamentoController {

    private final DepartamentoService service;

    public DepartamentoController(DepartamentoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) String ciudad) {
        // Si viene parámetro ciudad, buscar por ciudad, sino traer todos
        if (ciudad != null && !ciudad.trim().isEmpty()) {
            return service.getByCiudad(ciudad);
        }
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Departamento departamento) {
        return service.create(departamento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Departamento departamento) {
        return service.update(id, departamento);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}