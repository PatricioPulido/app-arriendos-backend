package com.apparriendos.arriendos_api.service;

import com.apparriendos.arriendos_api.error.ApiException;
import com.apparriendos.arriendos_api.mapper.DepartamentoMapper;
import com.apparriendos.arriendos_api.mapper.ImagenMapper;
import com.apparriendos.arriendos_api.model.Departamento;
import com.apparriendos.arriendos_api.model.FiltroDepartamentos;
import com.apparriendos.arriendos_api.model.Imagen;
import com.apparriendos.arriendos_api.security.UsuarioActual;
import com.apparriendos.arriendos_api.storage.ImageStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Propiedades. Las crea un ANFITRION (o ADMIN) y solo su dueno o un ADMIN puede editarlas
 * o borrarlas. Los errores de BD los traduce AuthExceptionHandler -> DbErrorHandler.
 */
@Service
public class DepartamentoService {

    private final DepartamentoMapper mapper;
    private final ImagenMapper imagenMapper;
    private final ImageStorage storage;

    public DepartamentoService(DepartamentoMapper mapper, ImagenMapper imagenMapper, ImageStorage storage) {
        this.mapper = mapper;
        this.imagenMapper = imagenMapper;
        this.storage = storage;
    }

    public List<Departamento> buscar(FiltroDepartamentos filtro) {
        if (filtro.llegada() != null && filtro.salida() != null && !filtro.llegada().isBefore(filtro.salida())) {
            throw ApiException.invalido("La fecha de salida debe ser posterior a la de llegada");
        }
        return mapper.buscar(filtro);
    }

    public Departamento detalle(Long id) {
        Departamento d = mapper.findById(id);
        if (d == null) throw ApiException.noEncontrado("Departamento no encontrado con id " + id);
        d.setImagenes(imagenMapper.findByDepartamento(id));
        return d;
    }

    /** Propiedades del anfitrion logueado, con sus fotos. */
    public List<Departamento> mios(UsuarioActual usuario) {
        List<Departamento> lista = mapper.findByAnfitrion(usuario.idUsuario());
        lista.forEach(d -> d.setImagenes(imagenMapper.findByDepartamento(d.getIdDepartamento())));
        return lista;
    }

    @Transactional
    public Departamento crear(Departamento d, UsuarioActual usuario) {
        d.setIdDepartamento(null); // lo asigna la secuencia
        d.setIdAnfitrion(usuario.idUsuario());
        normalizarFicha(d);
        mapper.insert(d);
        return detalle(d.getIdDepartamento());
    }

    @Transactional
    public Departamento actualizar(Long id, Departamento cambios, UsuarioActual usuario) {
        Departamento actual = buscarEditable(id, usuario);
        cambios.setIdDepartamento(id);
        // Si el cliente no envia las listas (p. ej. un front antiguo), se conservan las que habia
        if (cambios.getComodidades() == null) cambios.setComodidades(actual.getComodidades());
        if (cambios.getDestacados() == null) cambios.setDestacados(actual.getDestacados());
        normalizarFicha(cambios);
        mapper.update(cambios);
        return detalle(id);
    }

    @Transactional
    public void eliminar(Long id, UsuarioActual usuario) {
        buscarEditable(id, usuario);
        List<String> rutas = imagenMapper.findByDepartamento(id).stream().map(Imagen::getRuta).toList();
        mapper.delete(id); // las filas de fotos se borran en cascada
        // Los archivos se borran solo si la transaccion se confirma
        if (!rutas.isEmpty()) despuesDeConfirmar(() -> storage.borrar(rutas));
    }

    /** Devuelve la propiedad si existe y el usuario puede editarla; si no, 404 o 403. */
    public Departamento buscarEditable(Long id, UsuarioActual usuario) {
        Departamento d = mapper.findById(id);
        if (d == null) throw ApiException.noEncontrado("Departamento no encontrado con id " + id);
        if (!usuario.puedeEditar(d.getIdAnfitrion())) {
            throw ApiException.prohibido("Solo el anfitrión de esta propiedad puede modificarla");
        }
        return d;
    }

    /** Quita repetidos y textos vacios antes de guardar. */
    static void normalizarFicha(Departamento d) {
        d.setComodidades(sinRepetidos(d.getComodidades()));
        d.setDestacados(sinRepetidos(d.getDestacados()));
        d.setHoraCheckIn(textoONull(d.getHoraCheckIn()));
        d.setHoraCheckOut(textoONull(d.getHoraCheckOut()));
        d.setReglasAdicionales(textoONull(d.getReglasAdicionales()));
        d.setSobreLaZona(textoONull(d.getSobreLaZona()));
    }

    private static List<String> sinRepetidos(List<String> codigos) {
        return codigos == null ? List.of() : List.copyOf(new LinkedHashSet<>(codigos));
    }

    private static String textoONull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static void despuesDeConfirmar(Runnable accion) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    accion.run();
                }
            });
        } else {
            accion.run();
        }
    }
}
