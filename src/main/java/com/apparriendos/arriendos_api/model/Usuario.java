package com.apparriendos.arriendos_api.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Usuario {
    private Long idUsuario;
    private String email;
    private String passwordHash;
    private Rol rol;
    private Long idArrendatario;
    private boolean activo = true;
    private LocalDateTime fechaCreacion;
}
