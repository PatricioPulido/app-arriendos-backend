package com.apparriendos.arriendos_api.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Guarda las fotos en una carpeta local. Pensado para desarrollo (en Render el disco se borra). */
public class LocalImageStorage implements ImageStorage {

    private static final Logger logger = LoggerFactory.getLogger(LocalImageStorage.class);

    private final Path raiz;
    private final String urlBase;

    public LocalImageStorage(String carpeta, String urlBase) {
        this.raiz = Path.of(carpeta).toAbsolutePath().normalize();
        this.urlBase = urlBase.replaceAll("/+$", "");
    }

    public Path getRaiz() {
        return raiz;
    }

    @Override
    public String guardar(String ruta, byte[] datos, String contentType) {
        Path destino = resolver(ruta);
        try {
            Files.createDirectories(destino.getParent());
            Files.write(destino, datos);
        } catch (IOException e) {
            throw new StorageException("No se pudo guardar la foto", e);
        }
        return urlBase + "/" + ruta;
    }

    @Override
    public void borrar(List<String> rutas) {
        for (String ruta : rutas) {
            try {
                Files.deleteIfExists(resolver(ruta));
            } catch (IOException e) {
                logger.warn("No se pudo borrar {}: {}", ruta, e.getMessage());
            }
        }
    }

    /** Evita que una ruta con "../" salga de la carpeta. */
    private Path resolver(String ruta) {
        Path p = raiz.resolve(ruta).normalize();
        if (!p.startsWith(raiz)) throw new IllegalArgumentException("Ruta inválida");
        return p;
    }
}
