package com.apparriendos.arriendos_api.service;

import com.apparriendos.arriendos_api.error.ApiException;
import com.apparriendos.arriendos_api.mapper.ImagenMapper;
import com.apparriendos.arriendos_api.model.Departamento;
import com.apparriendos.arriendos_api.model.Imagen;
import com.apparriendos.arriendos_api.security.UsuarioActual;
import com.apparriendos.arriendos_api.storage.ImageStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ImagenServiceTest {

    ImagenMapper mapper = mock(ImagenMapper.class);
    DepartamentoService deptos = mock(DepartamentoService.class);
    ImageStorage storage = mock(ImageStorage.class);
    ImagenService service = new ImagenService(mapper, deptos, storage);
    UsuarioActual anfitrion = new UsuarioActual(3L, List.of("ANFITRION"), 60L);

    static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0};

    @BeforeEach
    void setUp() {
        when(deptos.buscarEditable(eq(1L), any())).thenReturn(new Departamento());
        when(mapper.maxOrden(1L)).thenReturn(-1);
        when(storage.guardar(anyString(), any(), anyString())).thenAnswer(inv -> "https://cdn/" + inv.getArgument(0));
    }

    private static MultipartFile archivo(String nombre, String tipo, byte[] datos) {
        return new MockMultipartFile("archivos", nombre, tipo, datos);
    }

    @Test
    void subeJpgYPngConRutaPropiaYOrden() {
        service.subir(1L, List.of(archivo("a.jpg", "image/jpeg", JPG), archivo("b.png", "image/png", PNG)), anfitrion);
        verify(storage).guardar(matches("departamentos/1/[0-9a-f-]{36}\\.jpg"), eq(JPG), eq("image/jpeg"));
        verify(storage).guardar(matches("departamentos/1/[0-9a-f-]{36}\\.png"), eq(PNG), eq("image/png"));
        verify(mapper).insert(argThat(i -> i.getOrden() == 0));
        verify(mapper).insert(argThat(i -> i.getOrden() == 1));
    }

    @Test
    void rechazaArchivoQueNoEsImagenAunqueDigaQueLoEs() {
        var falso = archivo("virus.jpg", "image/jpeg", "<script>alert(1)</script>".getBytes());
        assertThatThrownBy(() -> service.subir(1L, List.of(falso), anfitrion))
                .isInstanceOf(ApiException.class).hasMessageContaining("JPG, PNG o WebP");
        verifyNoInteractions(storage);
    }

    @Test
    void respetaElMaximoDeFotos() {
        when(mapper.findByDepartamento(1L)).thenReturn(Collections.nCopies(20, new Imagen()));
        assertThatThrownBy(() -> service.subir(1L, List.of(archivo("a.jpg", "image/jpeg", JPG)), anfitrion))
                .isInstanceOf(ApiException.class).hasMessageContaining("Máximo 20");
    }

    @Test
    void siFallaLaBdSeBorranLosArchivosSubidos() {
        doThrow(new RuntimeException("bd caida")).when(mapper).insert(any());
        assertThatThrownBy(() -> service.subir(1L, List.of(archivo("a.jpg", "image/jpeg", JPG)), anfitrion))
                .isInstanceOf(RuntimeException.class);
        verify(storage).borrar(argThat(l -> l.size() == 1));
    }

    @Test
    void noSePuedeBorrarFotoDeOtraPropiedad() {
        Imagen ajena = new Imagen();
        ajena.setIdImagen(9L);
        ajena.setIdDepartamento(2L);
        when(mapper.findById(9L)).thenReturn(ajena);
        assertThatThrownBy(() -> service.borrar(1L, 9L, anfitrion)).isInstanceOf(ApiException.class);
        verify(mapper, never()).delete(any());
    }

    @Test
    void ordenarExigeLasMismasFotos() {
        Imagen a = new Imagen(); a.setIdImagen(1L);
        Imagen b = new Imagen(); b.setIdImagen(2L);
        when(mapper.findByDepartamento(1L)).thenReturn(List.of(a, b));
        assertThatThrownBy(() -> service.ordenar(1L, List.of(1L, 3L), anfitrion)).isInstanceOf(ApiException.class);
        service.ordenar(1L, List.of(2L, 1L), anfitrion);
        verify(mapper).updateOrden(1L, 2L, 0);
        verify(mapper).updateOrden(1L, 1L, 1);
    }

    @Test
    void detectaFormatos() {
        assertThat(ImagenService.TipoImagen.detectar(JPG)).isEqualTo(ImagenService.TipoImagen.JPEG);
        assertThat(ImagenService.TipoImagen.detectar(PNG)).isEqualTo(ImagenService.TipoImagen.PNG);
        byte[] webp = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};
        assertThat(ImagenService.TipoImagen.detectar(webp)).isEqualTo(ImagenService.TipoImagen.WEBP);
        assertThat(ImagenService.TipoImagen.detectar("GIF89a".getBytes())).isNull();
    }
}
