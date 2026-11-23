package com.apparriendos.arriendos_api.model;

import java.time.LocalDate;

/**
 * Filtros de busqueda del catalogo. Todos son opcionales; los que vienen null no filtran.
 * Para sumar un filtro nuevo: agregar el campo aqui y su condicion en DepartamentoMapper.xml (buscar).
 */
public record FiltroDepartamentos(
        String texto,          // busca en ciudad, zona, nombre y direccion (lo que escribe en "¿A dónde vas?")
        Long idCiudad,
        Long idZona,
        Long precioMin,
        Long precioMax,
        Integer huespedes,     // capacidad >= huespedes
        Integer dormitorios,   // dormitorios >= dormitorios
        LocalDate llegada,     // con salida: solo propiedades libres en esas fechas
        LocalDate salida,
        String orden,          // "precio_asc" | "precio_desc" | "recientes" (por defecto)
        int limite,
        int offset) {

    /** Patron para ILIKE con los comodines del usuario escapados. */
    public String textoLike() {
        if (texto == null || texto.isBlank()) return null;
        String limpio = texto.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + limpio + "%";
    }
}
