package com.apparriendos.arriendos_api.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Ciudad {
    private Long idCiudad;

    @NotBlank(message = "El nombre de la ciudad no puede estar vacío")
    private String nombre;
}
