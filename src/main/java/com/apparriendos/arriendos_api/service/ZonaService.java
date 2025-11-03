package com.apparriendos.arriendos_api.service;

import com.apparriendos.arriendos_api.mapper.ZonaMapper;
import com.apparriendos.arriendos_api.model.Zona;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ZonaService {
    private final ZonaMapper mapper;
    private final DbErrorHandler dbErrorHandler;
    private final Logger logger = LoggerFactory.getLogger(ZonaService.class);

    public ZonaService(ZonaMapper mapper, DbErrorHandler dbErrorHandler) {
        this.mapper = mapper;
        this.dbErrorHandler = dbErrorHandler;
    }

    public ResponseEntity<?> getAll() {
        try {
            List<Zona> list = mapper.findAll();
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            logger.error("Error listar zonas", ex);
            return dbErrorHandler.handleException(ex, "Listar zonas");
        }
    }

    public ResponseEntity<?> getByCiudad(Long ciudadId) {
        try {
            List<Zona> list = mapper.findByCiudad(ciudadId);
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            logger.error("Error listar zonas por ciudad", ex);
            return dbErrorHandler.handleException(ex, "Listar zonas por ciudad");
        }
    }

    public ResponseEntity<?> getById(Long id) {
        try {
            Zona z = mapper.findById(id);
            if (z == null) return ResponseEntity.status(404).body(Map.of("mensaje", "Zona no encontrada"));
            return ResponseEntity.ok(z);
        } catch (Exception ex) {
            logger.error("Error obtener zona", ex);
            return dbErrorHandler.handleException(ex, "Obtener zona");
        }
    }

    public ResponseEntity<?> create(Zona zona) {
        try {
            zona.setIdZona(null);
            mapper.insert(zona);
            return ResponseEntity.status(201).body(Map.of("mensaje", "Zona creada", "id", zona.getIdZona()));
        } catch (Exception ex) {
            logger.error("Error crear zona", ex);
            return dbErrorHandler.handleException(ex, "Crear zona");
        }
    }

    public ResponseEntity<?> update(Long id, Zona zona) {
        try {
            zona.setIdZona(id);
            mapper.update(zona);
            return ResponseEntity.ok(Map.of("mensaje", "Zona actualizada"));
        } catch (Exception ex) {
            logger.error("Error actualizar zona", ex);
            return dbErrorHandler.handleException(ex, "Actualizar zona");
        }
    }

    public ResponseEntity<?> delete(Long id) {
        try {
            mapper.delete(id);
            return ResponseEntity.ok(Map.of("mensaje", "Zona eliminada"));
        } catch (Exception ex) {
            logger.error("Error eliminar zona", ex);
            return dbErrorHandler.handleException(ex, "Eliminar zona");
        }
    }
}
