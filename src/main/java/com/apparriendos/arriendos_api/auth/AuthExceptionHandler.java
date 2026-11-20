package com.apparriendos.arriendos_api.auth;

import com.apparriendos.arriendos_api.error.ApiException;
import com.apparriendos.arriendos_api.service.DbErrorHandler;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Convierte las excepciones en respuestas JSON { mensaje, ... } para todos los controllers. */
@RestControllerAdvice
public class AuthExceptionHandler {

    private final DbErrorHandler dbErrorHandler;

    public AuthExceptionHandler(DbErrorHandler dbErrorHandler) {
        this.dbErrorHandler = dbErrorHandler;
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuth(AuthException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("mensaje", ex.getMessage()));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("mensaje", ex.getMessage()));
    }

    /** Errores de @Valid en cualquier controller: { mensaje, errores: { campo: motivo } } */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("mensaje", "Datos inválidos", "errores", errores));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> handleBadInput(Exception ex) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", "Datos inválidos en la petición"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleTooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("mensaje", "Las fotos pesan demasiado (máximo 5 MB cada una)"));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, String>> handleMultipart(MultipartException ex) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", "No se pudieron leer los archivos enviados"));
    }

    @ExceptionHandler(com.apparriendos.arriendos_api.storage.StorageException.class)
    public ResponseEntity<Map<String, String>> handleStorage(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("mensaje", ex.getMessage()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> handleDb(DataAccessException ex) {
        return dbErrorHandler.handleException(ex, "Operación en base de datos");
    }
}
