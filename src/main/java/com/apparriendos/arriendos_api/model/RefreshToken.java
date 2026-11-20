package com.apparriendos.arriendos_api.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RefreshToken {
    private Long idRefreshToken;
    private Long idUsuario;
    private String tokenHash;
    private LocalDateTime expira;
    private boolean revocado;
    private LocalDateTime fechaRevocacion;
}
