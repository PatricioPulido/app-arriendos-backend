package com.apparriendos.arriendos_api.service;

import com.apparriendos.arriendos_api.mapper.ArrendatarioMapper;
import com.apparriendos.arriendos_api.model.Arrendatario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ArrendatarioService {

    private final ArrendatarioMapper mapper;
    private final DbErrorHandler dbErrorHandler;
    private final Logger logger = LoggerFactory.getLogger(ArrendatarioService.class);

    public ArrendatarioService(ArrendatarioMapper mapper, DbErrorHandler dbErrorHandler) {
        this.mapper = mapper;
        this.dbErrorHandler = dbErrorHandler;
    }

    public ResponseEntity<?> getAll() {
        try {
            List<Arrendatario> list = mapper.findAll();
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            logger.error("Error getAll Arrendatario", ex);
            return dbErrorHandler.handleException(ex, "Listar arrendatarios");
        }
    }

    public ResponseEntity<?> getById(Long id) {
        try {
            Arrendatario a = mapper.findById(id);
            if (a == null) {
                return ResponseEntity.status(404).body(Map.of("mensaje", "Arrendatario no encontrado con id " + id));
            }
            return ResponseEntity.ok(a);
        } catch (Exception ex) {
            logger.error("Error getById Arrendatario", ex);
            return dbErrorHandler.handleException(ex, "Obtener arrendatario por id");
        }
    }

    public ResponseEntity<?> create(Arrendatario arrendatario) {
        try {
            // seguridad: ignorar cualquier id enviado por el cliente
            arrendatario.setIdArrendatario(null);

            // si tu driver no mapea LocalDate automáticamente, descomenta y convierte:
            // if (arrendatario.getFechaNacimiento() != null) {
            //     arrendatario.setFechaNacimiento(java.sql.Date.valueOf(arrendatario.getFechaNacimiento()).toLocalDate());
            // }

            mapper.insert(arrendatario); // selectKey asigna idArrendatario
            return ResponseEntity.status(201).body(Map.of(
                    "mensaje", "Arrendatario creado con éxito",
                    "id", arrendatario.getIdArrendatario()
            ));
        } catch (Exception ex) {
            logger.error("Error create Arrendatario", ex);
            return dbErrorHandler.handleException(ex, "Crear arrendatario");
        }
    }

    public ResponseEntity<?> update(Long id, Arrendatario arrendatario) {
        try {
            arrendatario.setIdArrendatario(id);
            mapper.update(arrendatario);
            return ResponseEntity.ok(Map.of("mensaje", "Arrendatario actualizado con éxito"));
        } catch (Exception ex) {
            logger.error("Error update Arrendatario", ex);
            return dbErrorHandler.handleException(ex, "Actualizar arrendatario");
        }
    }

    public ResponseEntity<?> delete(Long id) {
        try {
            mapper.delete(id);
            return ResponseEntity.ok(Map.of("mensaje", "Arrendatario eliminado con éxito"));
        } catch (Exception ex) {
            logger.error("Error delete Arrendatario", ex);
            return dbErrorHandler.handleException(ex, "Eliminar arrendatario");
        }
    }
}
