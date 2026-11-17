package com.apparriendos.arriendos_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class DbErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(DbErrorHandler.class);

    // Si es false (por defecto), no se envian al cliente mensajes SQL ni detalles internos
    @Value("${app.error.expose-details:false}")
    private boolean exposeDetails;

    public ResponseEntity<Map<String, String>> handleException(Exception ex, String action) {
        ResponseEntity<Map<String, String>> response = buildResponse(ex, action);
        logger.error("Error en '{}'", action, ex);
        if (!exposeDetails && response.getBody() != null) {
            response.getBody().remove("detalle");
            response.getBody().remove("codigo_sql");
        }
        return response;
    }

    private ResponseEntity<Map<String, String>> buildResponse(Exception ex, String action) {
        Map<String, String> error = new HashMap<>();

        error.put("accion", action);

        // 1) Errores de acceso a datos (Spring / MyBatis)
        if (ex instanceof DataAccessException dae) {
            Throwable rootCause = dae.getRootCause();

            if (rootCause instanceof SQLException sqlEx) {
                String sqlState = sqlEx.getSQLState();
                String message = sqlEx.getMessage();

                error.put("codigo_sql", sqlState);
                error.put("detalle", message);

                // Codigos SQLSTATE de PostgreSQL
                String estado = sqlState == null ? "" : sqlState;
                switch (estado) {
                    case "42P01": // tabla no existe
                        error.put("mensaje", "La tabla o vista referenciada no existe");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);

                    case "23505": // unique_violation
                        error.put("mensaje", "Violación de restricción única (duplicado)");
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

                    case "23503": // foreign_key_violation (padre inexistente o hijos existentes)
                        boolean tieneHijos = message != null && message.contains("still referenced");
                        error.put("mensaje", tieneHijos
                                ? "No se puede eliminar: existen registros relacionados"
                                : "Violación de clave foránea (registro padre inexistente)");
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

                    case "23514": // check_violation
                        error.put("mensaje", "Algún dato no cumple las reglas (por ejemplo fechas o estado)");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

                    default:
                        error.put("mensaje", "Error en base de datos (código " + estado + ")");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                }
            }

            // Caso genérico
            error.put("mensaje", "Error en acceso a datos");
            error.put("detalle", dae.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        // 2) Otros errores
        error.put("mensaje", "Error inesperado");
        error.put("detalle", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
