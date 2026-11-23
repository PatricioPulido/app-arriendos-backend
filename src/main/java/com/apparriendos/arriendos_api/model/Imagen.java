package com.apparriendos.arriendos_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/** Foto de la galeria de una propiedad. */
@Data
public class Imagen {
    private Long idImagen;
    private Long idDepartamento;
    /** Ruta interna del archivo (bucket o carpeta local). No se envia al cliente. */
    @JsonIgnore
    private String ruta;
    private String url;
    private Integer orden;
}
