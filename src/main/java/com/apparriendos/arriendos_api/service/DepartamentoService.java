package com.apparriendos.arriendos_api.service;

import com.apparriendos.arriendos_api.mapper.DepartamentoMapper;
import com.apparriendos.arriendos_api.model.Departamento;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DepartamentoService {

    private final DepartamentoMapper mapper;
    private final DbErrorHandler dbErrorHandler;

    public DepartamentoService(DepartamentoMapper mapper, DbErrorHandler dbErrorHandler) {
        this.mapper = mapper;
        this.dbErrorHandler = dbErrorHandler;
    }

    public ResponseEntity<?> getAll() {
        try {
            List<Departamento> list = mapper.findAll();
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            return dbErrorHandler.handleException(ex, "Obtener todos los departamentos");
        }
    }

    public ResponseEntity<?> getById(Long id) {
        try {
            Departamento dep = mapper.findById(id);
            if (dep == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("mensaje", "Departamento no encontrado con id " + id));
            }
            return ResponseEntity.ok(dep);
        } catch (Exception ex) {
            return dbErrorHandler.handleException(ex, "Obtener departamento por id");
        }
    }

    public ResponseEntity<?> create(Departamento departamento) {
        try {
            mapper.insert(departamento);
            return ResponseEntity.status(201).body(Map.of("mensaje", "Departamento ", departamento.getNombre()," creado con éxito"));
        } catch (Exception ex) {
            return dbErrorHandler.handleException(ex, "Crear departamento");
        }
    }

    public ResponseEntity<?> update(Long id, Departamento departamento) {
        try {
            departamento.setIdDepartamento(id);
            mapper.update(departamento);
            return ResponseEntity.ok(Map.of("mensaje", "Departamento actualizado con éxito"));
        } catch (Exception ex) {
            return dbErrorHandler.handleException(ex, "Actualizar departamento");
        }
    }

    public ResponseEntity<?> delete(Long id) {
        try {
            mapper.delete(id);
            return ResponseEntity.ok(Map.of("mensaje", "Departamento eliminado con éxito"));
        } catch (Exception ex) {
            return dbErrorHandler.handleException(ex, "Eliminar departamento");
        }
    }
}
