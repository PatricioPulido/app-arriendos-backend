package com.apparriendos.arriendos_api.controller;

import com.apparriendos.arriendos_api.model.Departamento;
import com.apparriendos.arriendos_api.model.FiltroDepartamentos;
import com.apparriendos.arriendos_api.model.Imagen;
import com.apparriendos.arriendos_api.security.UsuarioActual;
import com.apparriendos.arriendos_api.service.DepartamentoService;
import com.apparriendos.arriendos_api.service.ImagenService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Catalogo publico (GET) y gestion de propiedades por su anfitrion.
 * Quien puede llamar cada ruta esta en SecurityConfig; que propiedad puede tocar, en el servicio.
 */
@RestController
@RequestMapping("/api/departamentos")
public class DepartamentoController {

    private final DepartamentoService service;
    private final ImagenService imagenes;

    public DepartamentoController(DepartamentoService service, ImagenService imagenes) {
        this.service = service;
        this.imagenes = imagenes;
    }

    /**
     * Busqueda con filtros opcionales, por ejemplo:
     * /api/departamentos?ciudad=viña&huespedes=4&precioMax=80000&llegada=2026-12-20&salida=2026-12-27&orden=precio_asc
     */
    @GetMapping
    public List<Departamento> buscar(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Long idCiudad,
            @RequestParam(required = false) Long idZona,
            @RequestParam(required = false) Long precioMin,
            @RequestParam(required = false) Long precioMax,
            @RequestParam(required = false) Integer huespedes,
            @RequestParam(required = false) Integer dormitorios,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate llegada,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salida,
            @RequestParam(required = false) String orden,
            @RequestParam(defaultValue = "50") int limite,
            @RequestParam(defaultValue = "0") int pagina) {
        int lim = Math.max(1, Math.min(limite, 100));
        int pag = Math.max(0, pagina);
        return service.buscar(new FiltroDepartamentos(ciudad, idCiudad, idZona, precioMin, precioMax,
                huespedes, dormitorios, llegada, salida, orden, lim, pag * lim));
    }

    /** Propiedades del anfitrion logueado. */
    @GetMapping("/mios")
    public List<Departamento> mios(@AuthenticationPrincipal Jwt jwt) {
        return service.mios(UsuarioActual.de(jwt));
    }

    @GetMapping("/{id}")
    public Departamento detalle(@PathVariable Long id) {
        return service.detalle(id);
    }

    @PostMapping
    public ResponseEntity<Departamento> crear(@Valid @RequestBody Departamento departamento,
                                              @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(departamento, UsuarioActual.de(jwt)));
    }

    @PutMapping("/{id}")
    public Departamento actualizar(@PathVariable Long id, @Valid @RequestBody Departamento departamento,
                                   @AuthenticationPrincipal Jwt jwt) {
        return service.actualizar(id, departamento, UsuarioActual.de(jwt));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        service.eliminar(id, UsuarioActual.de(jwt));
        return Map.of("mensaje", "Departamento eliminado con éxito");
    }

    // --- Galeria ---

    /** Sube una o varias fotos (campo "archivos" de un form multipart). */
    @PostMapping(path = "/{id}/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<Imagen>> subirImagenes(@PathVariable Long id,
                                                      @RequestParam("archivos") List<MultipartFile> archivos,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(imagenes.subir(id, archivos, UsuarioActual.de(jwt)));
    }

    @DeleteMapping("/{id}/imagenes/{idImagen}")
    public List<Imagen> borrarImagen(@PathVariable Long id, @PathVariable Long idImagen,
                                     @AuthenticationPrincipal Jwt jwt) {
        return imagenes.borrar(id, idImagen, UsuarioActual.de(jwt));
    }

    /** Body: [idImagen, idImagen, ...] en el orden deseado. La primera es la portada. */
    @PutMapping("/{id}/imagenes/orden")
    public List<Imagen> ordenarImagenes(@PathVariable Long id, @RequestBody List<Long> ids,
                                        @AuthenticationPrincipal Jwt jwt) {
        return imagenes.ordenar(id, ids, UsuarioActual.de(jwt));
    }
}
