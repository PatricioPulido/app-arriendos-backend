package com.apparriendos.arriendos_api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Zona {
    private Long idZona;

    @NotBlank(message = "El nombre de la zona no puede estar vacío")
    private String nombre;

    @NotNull(message = "El id de la ciudad es requerido")
    private Long idCiudad;
}
