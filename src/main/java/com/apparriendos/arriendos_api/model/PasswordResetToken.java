package com.apparriendos.arriendos_api.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PasswordResetToken {
    private Long idReset;
    private Long idUsuario;
    private String tokenHash;
    private LocalDateTime expira;
    private boolean usado;
    private LocalDateTime fechaCreacion;
}
