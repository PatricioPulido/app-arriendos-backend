package com.apparriendos.arriendos_api.auth;

import org.springframework.http.HttpStatus;

/** Error de login/registro con el estado HTTP que debe recibir el cliente. */
public class AuthException extends RuntimeException {
    private final HttpStatus status;

    public AuthException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
