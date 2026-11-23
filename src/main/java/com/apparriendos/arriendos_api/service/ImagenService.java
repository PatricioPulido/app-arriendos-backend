package com.apparriendos.arriendos_api.service;

import com.apparriendos.arriendos_api.error.ApiException;
import com.apparriendos.arriendos_api.mapper.ImagenMapper;
import com.apparriendos.arriendos_api.model.Imagen;
import com.apparriendos.arriendos_api.security.UsuarioActual;
import com.apparriendos.arriendos_api.storage.ImageStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/** Galeria de fotos de una propiedad: subir, borrar y ordenar. La primera es la portada. */
@Service
public class ImagenService {

    static final int MAX_FOTOS_POR_PROPIEDAD = 20;
    static final long MAX_BYTES = 5L * 1024 * 1024;

    private final ImagenMapper mapper;
    private final DepartamentoService departamentos;
    private final ImageStorage storage;

    public ImagenService(ImagenMapper mapper, DepartamentoService departamentos, ImageStorage storage) {
        this.mapper = mapper;
        this.departamentos = departamentos;
        this.storage = storage;
    }

    @Transactional
    public List<Imagen> subir(Long idDepartamento, List<MultipartFile> archivos, UsuarioActual usuario) {
        departamentos.buscarEditable(idDepartamento, usuario);
        if (archivos == null || archivos.isEmpty()) throw ApiException.invalido("Selecciona al menos una foto");

        int actuales = mapper.findByDepartamento(idDepartamento).size();
        if (actuales + archivos.size() > MAX_FOTOS_POR_PROPIEDAD) {
            throw ApiException.invalido("Máximo " + MAX_FOTOS_POR_PROPIEDAD + " fotos por propiedad (ya tiene " + actuales + ")");
        }

        // Primero se valida todo, para no subir la mitad y fallar con la otra mitad
        List<byte[]> datos = new ArrayList<>();
        List<TipoImagen> tipos = new ArrayList<>();
        for (MultipartFile f : archivos) {
            if (f.isEmpty()) throw ApiException.invalido("Uno de los archivos está vacío");
            if (f.getSize() > MAX_BYTES) throw ApiException.invalido("Cada foto puede pesar hasta 5 MB");
            byte[] bytes;
            try {
                bytes = f.getBytes();
            } catch (IOException e) {
                throw ApiException.invalido("No se pudo leer el archivo");
            }
            TipoImagen tipo = TipoImagen.detectar(bytes);
            if (tipo == null) throw ApiException.invalido("Solo se aceptan fotos JPG, PNG o WebP");
            datos.add(bytes);
            tipos.add(tipo);
        }

        int orden = mapper.maxOrden(idDepartamento);
        List<String> subidas = new ArrayList<>();
        try {
            for (int i = 0; i < datos.size(); i++) {
                TipoImagen tipo = tipos.get(i);
                String ruta = "departamentos/" + idDepartamento + "/" + UUID.randomUUID() + "." + tipo.extension;
                String url = storage.guardar(ruta, datos.get(i), tipo.mime);
                subidas.add(ruta);

                Imagen img = new Imagen();
                img.setIdDepartamento(idDepartamento);
                img.setRuta(ruta);
                img.setUrl(url);
                img.setOrden(++orden);
                mapper.insert(img);
            }
        } catch (RuntimeException e) {
            storage.borrar(subidas); // no dejar archivos sin fila en la BD
            throw e;
        }
        return mapper.findByDepartamento(idDepartamento);
    }

    @Transactional
    public List<Imagen> borrar(Long idDepartamento, Long idImagen, UsuarioActual usuario) {
        departamentos.buscarEditable(idDepartamento, usuario);
        Imagen img = mapper.findById(idImagen);
        if (img == null || !img.getIdDepartamento().equals(idDepartamento)) {
            throw ApiException.noEncontrado("Foto no encontrada");
        }
        mapper.delete(idImagen);
        DepartamentoService.despuesDeConfirmar(() -> storage.borrar(List.of(img.getRuta())));
        return mapper.findByDepartamento(idDepartamento);
    }

    /** Recibe los ids en el orden deseado; el primero queda de portada. */
    @Transactional
    public List<Imagen> ordenar(Long idDepartamento, List<Long> ids, UsuarioActual usuario) {
        departamentos.buscarEditable(idDepartamento, usuario);
        List<Imagen> actuales = mapper.findByDepartamento(idDepartamento);
        var esperados = new HashSet<>(actuales.stream().map(Imagen::getIdImagen).toList());
        if (ids == null || ids.size() != esperados.size() || !esperados.equals(new HashSet<>(ids))) {
            throw ApiException.invalido("La lista debe tener exactamente las fotos de la propiedad");
        }
        for (int i = 0; i < ids.size(); i++) {
            mapper.updateOrden(idDepartamento, ids.get(i), i);
        }
        return mapper.findByDepartamento(idDepartamento);
    }

    /** Se reconoce el formato por los primeros bytes, no por el nombre ni el Content-Type que manda el navegador. */
    enum TipoImagen {
        JPEG("image/jpeg", "jpg"), PNG("image/png", "png"), WEBP("image/webp", "webp");

        final String mime;
        final String extension;

        TipoImagen(String mime, String extension) {
            this.mime = mime;
            this.extension = extension;
        }

        static TipoImagen detectar(byte[] b) {
            if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return JPEG;
            if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                    && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) return PNG;
            if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return WEBP;
            return null;
        }
    }
}
