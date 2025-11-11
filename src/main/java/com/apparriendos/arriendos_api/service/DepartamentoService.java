package com.apparriendos.arriendos_api.service;

import com.apparriendos.arriendos_api.mapper.DepartamentoMapper;
import com.apparriendos.arriendos_api.model.Departamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DepartamentoService {
    private final DepartamentoMapper mapper;
    private final DbErrorHandler dbErrorHandler;
    private final Logger logger = LoggerFactory.getLogger(DepartamentoService.class);

    public DepartamentoService(DepartamentoMapper mapper, DbErrorHandler dbErrorHandler) {
        this.mapper = mapper;
        this.dbErrorHandler = dbErrorHandler;
    }

    public ResponseEntity<?> getAll() {
        try {
            List<Departamento> list = mapper.findAll();
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            logger.error("Error listar departamentos", ex);
            return dbErrorHandler.handleException(ex, "Listar departamentos");
        }
    }

    // NUEVO MÉTODO PARA BUSCAR POR CIUDAD
    public ResponseEntity<?> getByCiudad(String ciudad) {
        try {
            List<Departamento> list = mapper.findByCiudad(ciudad);
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            logger.error("Error buscar departamentos por ciudad", ex);
            return dbErrorHandler.handleException(ex, "Buscar departamentos por ciudad");
        }
    }

    public ResponseEntity<?> getById(Long id) {
        try {
            Departamento d = mapper.findById(id);
            if (d == null)
                return ResponseEntity.status(404).body(Map.of("mensaje", "Departamento no encontrado"));
            return ResponseEntity.ok(d);
        } catch (Exception ex) {
            logger.error("Error obtener departamento", ex);
            return dbErrorHandler.handleException(ex, "Obtener departamento");
        }
    }

    public ResponseEntity<?> create(Departamento departamento) {
        try {
            departamento.setIdDepartamento(null);
            mapper.insert(departamento);
            return ResponseEntity.status(201)
                    .body(Map.of("mensaje", "Departamento creado", "id", departamento.getIdDepartamento()));
        } catch (Exception ex) {
            logger.error("Error crear departamento", ex);
            return dbErrorHandler.handleException(ex, "Crear departamento");
        }
    }

    public ResponseEntity<?> update(Long id, Departamento departamento) {
        try {
            departamento.setIdDepartamento(id);
            mapper.update(departamento);
            return ResponseEntity.ok(Map.of("mensaje", "Departamento actualizado"));
        } catch (Exception ex) {
            logger.error("Error actualizar departamento", ex);
            return dbErrorHandler.handleException(ex, "Actualizar departamento");
        }
    }

    public ResponseEntity<?> delete(Long id) {
        try {
            mapper.delete(id);
            return ResponseEntity.ok(Map.of("mensaje", "Departamento eliminado"));
        } catch (Exception ex) {
            logger.error("Error eliminar departamento", ex);
            return dbErrorHandler.handleException(ex, "Eliminar departamento");
        }
    }
}