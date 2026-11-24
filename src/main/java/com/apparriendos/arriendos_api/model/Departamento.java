package com.apparriendos.arriendos_api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Departamento {
    static final String CODIGO = "^[a-z0-9_]{1,40}$";
    /** HH:MM o vacio (vacio se guarda como null). */
    static final String HORA = "^(([01][0-9]|2[0-3]):[0-5][0-9])?$";

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

    // --- Ficha estilo Airbnb (migracion 004). Los codigos y sus iconos estan en el front. ---

    /** Codigos de comodidades: "wifi", "piscina", "estacionamiento"... */
    @Size(max = 80, message = "Puedes marcar hasta 80 comodidades")
    private List<@Pattern(regexp = CODIGO, message = "Código de comodidad inválido") String> comodidades;

    /** "Lo mejor de este lugar": hasta 3 codigos ("llegada_autonoma", "vista_al_mar"...). */
    @Size(max = 3, message = "Elige hasta 3 puntos destacados")
    private List<@Pattern(regexp = CODIGO, message = "Código de destacado inválido") String> destacados;

    @Pattern(regexp = HORA, message = "Usa el formato HH:MM, por ejemplo 15:00")
    private String horaCheckIn;
    @Pattern(regexp = HORA, message = "Usa el formato HH:MM, por ejemplo 11:00")
    private String horaCheckOut;

    /** Reglas de la casa. null = el anfitrion no lo indico. */
    private Boolean permiteMascotas;
    private Boolean permiteFumar;
    private Boolean permiteFiestas;

    @Size(max = 1000, message = "Las reglas adicionales pueden tener hasta 1000 caracteres")
    private String reglasAdicionales;

    /** Que hay cerca: playa, restaurantes, transporte... */
    @Size(max = 1000, message = "El texto sobre la zona puede tener hasta 1000 caracteres")
    private String sobreLaZona;

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
