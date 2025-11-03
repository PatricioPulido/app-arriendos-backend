package com.apparriendos.arriendos_api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class Departamento {
    private Long idDepartamento;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @Positive(message = "El número debe ser positivo")
    private Integer numero;

    private String direccion;
    private Double metrosCuadrados;
    private String tipologia;
}
