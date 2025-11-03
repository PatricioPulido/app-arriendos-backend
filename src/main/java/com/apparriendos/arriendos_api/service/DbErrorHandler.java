package com.apparriendos.arriendos_api.service;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class DbErrorHandler {

    public ResponseEntity<Map<String, String>> handleException(Exception ex, String action) {
        Map<String, String> error = new HashMap<>();

        error.put("accion", action);

        // 1) Errores de acceso a datos (Spring / MyBatis)
        if (ex instanceof DataAccessException dae) {
            Throwable rootCause = dae.getRootCause();

            if (rootCause instanceof SQLException sqlEx) {
                String sqlState = sqlEx.getSQLState();
                int errorCode = sqlEx.getErrorCode();
                String message = sqlEx.getMessage();

                error.put("codigo_sql", String.valueOf(errorCode));
                error.put("detalle", message);

                // Ejemplos de códigos Oracle
                switch (errorCode) {
                    case 942: // ORA-00942: tabla o vista no existe
                        error.put("mensaje", "La tabla o vista referenciada no existe");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);

                    case 1: // ORA-00001: unique constraint violated
                        error.put("mensaje", "Violación de restricción única (duplicado)");
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

                    case 2291: // ORA-02291: integridad referencial (padre inexistente)
                        error.put("mensaje", "Violación de clave foránea (registro padre inexistente)");
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

                    case 2292: // ORA-02292: integridad referencial (hijos existentes)
                        error.put("mensaje", "No se puede eliminar: existen registros relacionados");
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

                    default:
                        error.put("mensaje", "Error en base de datos (código ORA-" + errorCode + ")");
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
