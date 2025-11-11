package com.apparriendos.arriendos_api.service;

import com.apparriendos.arriendos_api.mapper.CiudadMapper;
import com.apparriendos.arriendos_api.model.Ciudad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CiudadService {
    private final CiudadMapper mapper;
    private final DbErrorHandler dbErrorHandler;
    private final Logger logger = LoggerFactory.getLogger(CiudadService.class);

    public CiudadService(CiudadMapper mapper, DbErrorHandler dbErrorHandler) {
        this.mapper = mapper;
        this.dbErrorHandler = dbErrorHandler;
    }

    public ResponseEntity<?> getAll() {
        try {
            List<Ciudad> list = mapper.findAll();
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            logger.error("Error listar ciudades", ex);
            return dbErrorHandler.handleException(ex, "Listar ciudades");
        }
    }

    public ResponseEntity<?> getById(Long id) {
        try {
            Ciudad c = mapper.findById(id);
            if (c == null)
                return ResponseEntity.status(404).body(Map.of("mensaje", "Ciudad no encontrada"));
            return ResponseEntity.ok(c);
        } catch (Exception ex) {
            logger.error("Error obtener ciudad", ex);
            return dbErrorHandler.handleException(ex, "Obtener ciudad");
        }
    }

    public ResponseEntity<?> search(String nombre) {
        try {
            List<Ciudad> list = mapper.searchByNombre(nombre);
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            logger.error("Error buscar ciudades", ex);
            return dbErrorHandler.handleException(ex, "Buscar ciudades");
        }
    }

    public ResponseEntity<?> create(Ciudad ciudad) {
        try {
            ciudad.setIdCiudad(null);
            mapper.insert(ciudad);
            return ResponseEntity.status(201)
                    .body(Map.of("mensaje", "Ciudad creada", "id", ciudad.getIdCiudad()));
        } catch (Exception ex) {
            logger.error("Error crear ciudad", ex);
            return dbErrorHandler.handleException(ex, "Crear ciudad");
        }
    }

    public ResponseEntity<?> update(Long id, Ciudad ciudad) {
        try {
            ciudad.setIdCiudad(id);
            mapper.update(ciudad);
            return ResponseEntity.ok(Map.of("mensaje", "Ciudad actualizada"));
        } catch (Exception ex) {
            logger.error("Error actualizar ciudad", ex);
            return dbErrorHandler.handleException(ex, "Actualizar ciudad");
        }
    }

    public ResponseEntity<?> delete(Long id) {
        try {
            mapper.delete(id);
            return ResponseEntity.ok(Map.of("mensaje", "Ciudad eliminada"));
        } catch (Exception ex) {
            logger.error("Error eliminar ciudad", ex);
            return dbErrorHandler.handleException(ex, "Eliminar ciudad");
        }
    }
}
