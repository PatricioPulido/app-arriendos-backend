package com.apparriendos.arriendos_api.storage;

import java.util.List;

/**
 * Donde se guardan los archivos de las fotos.
 * - Produccion: Supabase Storage (bucket publico, sube el backend con la secret key).
 * - Local: carpeta en disco servida por el propio backend en /uploads/**.
 */
public interface ImageStorage {

    /** Guarda el archivo y devuelve su URL publica. */
    String guardar(String ruta, byte[] datos, String contentType);

    /** Borra los archivos. No falla si alguno ya no existe. */
    void borrar(List<String> rutas);
}
