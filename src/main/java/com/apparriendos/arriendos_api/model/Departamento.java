package com.apparriendos.arriendos_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Departamento {
    private Long idDepartamento;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 150, message = "El nombre puede tener hasta 150 caracteres")
    private String nombre;

    @Size(max = 2000, message = "La descripción puede tener hasta 2000 caracteres")
    private String descripcion;

    @Positive(message = "El número debe ser positivo")
    private Integer numero;

    @Size(max = 255)
    private String direccion;
    @PositiveOrZero(message = "Los metros cuadrados no pueden ser negativos")
    private Double metrosCuadrados;
    @Size(max = 50)
    private String tipologia;

    @Positive(message = "La capacidad debe ser al menos 1 huésped")
    @Max(value = 50, message = "La capacidad máxima es 50 huéspedes")
    private Integer capacidad;
    @PositiveOrZero @Max(50)
    private Integer dormitorios;
    @PositiveOrZero @Max(50)
    private Integer banos;

    /** Zona (y por ella la ciudad) donde esta. Base de los filtros de busqueda. */
    private Long idZona;

    /**
     * URL de la portada. Si la propiedad tiene fotos en la galeria, es la primera;
     * si no, la URL escrita a mano (compatibilidad con el panel admin antiguo).
     */
    @Size(max = 500)
    private String imagenUrl;

    @PositiveOrZero(message = "El precio no puede ser negativo")
    private Long precio; // CLP por noche

    // --- Solo lectura: los pone el servidor, se ignoran si vienen en el body ---
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long idAnfitrion;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String nombreAnfitrion;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String nombreZona;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long idCiudad;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String nombreCiudad;
    /** Solo en el detalle (GET /api/departamentos/{id}) y en "mis propiedades". */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Imagen> imagenes;
}
