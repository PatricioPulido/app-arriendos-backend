package com.apparriendos.arriendos_api.auth;

import com.apparriendos.arriendos_api.mail.EmailSender;
import com.apparriendos.arriendos_api.mapper.PasswordResetMapper;
import com.apparriendos.arriendos_api.mapper.RefreshTokenMapper;
import com.apparriendos.arriendos_api.mapper.UsuarioMapper;
import com.apparriendos.arriendos_api.model.PasswordResetToken;
import com.apparriendos.arriendos_api.model.Rol;
import com.apparriendos.arriendos_api.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    UsuarioMapper usuarios = mock(UsuarioMapper.class);
    PasswordResetMapper resets = mock(PasswordResetMapper.class);
    RefreshTokenMapper refresh = mock(RefreshTokenMapper.class);
    EmailSender correo = mock(EmailSender.class);
    PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    Clock clock = Clock.fixed(Instant.parse("2026-11-24T12:00:00Z"), ZoneOffset.UTC);
    LocalDateTime ahora = LocalDateTime.now(clock);
    PasswordResetService service;
    Usuario usuario;

    @BeforeEach
    void setUp() {
        // Executor que corre en el mismo hilo para poder verificar el correo
        service = new PasswordResetService(usuarios, resets, refresh, encoder, correo, Runnable::run,
                "https://front.test/", "ArriendoChile", clock);
        usuario = new Usuario();
        usuario.setIdUsuario(7L);
        usuario.setEmail("ana@test.cl");
        usuario.setPasswordHash(encoder.encode("clave-vieja"));
        usuario.setRol(Rol.ADMIN);
        usuario.setActivo(true);
        when(usuarios.findByEmail("ana@test.cl")).thenReturn(usuario);
        when(usuarios.findById(7L)).thenReturn(usuario);
    }

    /** Pide un enlace y devuelve el token que viaja en el correo. */
    private String pedirEnlace() {
        service.solicitar("  Ana@Test.cl ");
        ArgumentCaptor<String> texto = ArgumentCaptor.forClass(String.class);
        verify(correo).enviar(eq("ana@test.cl"), anyString(), anyString(), texto.capture());
        String marca = "https://front.test/restablecer-contrasena?token=";
        int i = texto.getValue().indexOf(marca);
        assertThat(i).isGreaterThanOrEqualTo(0);
        return texto.getValue().substring(i + marca.length()).split("\\s")[0];
    }

    private PasswordResetToken guardado(String token, LocalDateTime expira, boolean usado) {
        PasswordResetToken t = new PasswordResetToken();
        t.setIdReset(1L);
        t.setIdUsuario(7L);
        t.setTokenHash(AuthService.sha256(token));
        t.setExpira(expira);
        t.setUsado(usado);
        when(resets.findByHash(AuthService.sha256(token))).thenReturn(t);
        return t;
    }

    @Test
    void solicitarGuardaSoloElHashYEnviaElEnlace() {
        String token = pedirEnlace();
        ArgumentCaptor<PasswordResetToken> t = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(resets).insert(t.capture());
        assertThat(t.getValue().getTokenHash()).isEqualTo(AuthService.sha256(token)).isNotEqualTo(token);
        assertThat(t.getValue().getExpira()).isEqualTo(ahora.plusMinutes(30));
    }

    @Test
    void emailInexistenteNoEnviaNada() {
        service.solicitar("nadie@test.cl");
        verifyNoInteractions(correo);
        verify(resets, never()).insert(any());
    }

    @Test
    void usuarioInactivoNoRecibeEnlace() {
        usuario.setActivo(false);
        service.solicitar("ana@test.cl");
        verifyNoInteractions(correo);
    }

    @Test
    void maximoTresEnlacesPorHora() {
        when(resets.countDesde(7L, ahora.minusHours(1))).thenReturn(3);
        service.solicitar("ana@test.cl");
        verifyNoInteractions(correo);
        verify(resets, never()).insert(any());
    }

    @Test
    void restablecerCambiaLaClaveYCierraSesiones() {
        guardado("abc", ahora.plusMinutes(10), false);
        when(resets.marcarUsado(1L, ahora)).thenReturn(1);

        service.restablecer("abc", "clave-nueva-123");

        ArgumentCaptor<String> hash = ArgumentCaptor.forClass(String.class);
        verify(usuarios).updatePassword(eq(7L), hash.capture());
        assertThat(encoder.matches("clave-nueva-123", hash.getValue())).isTrue();
        verify(resets).anularPendientes(7L, ahora);
        verify(refresh).revokeAllForUser(7L);
    }

    @Test
    void enlaceVencidoNoSirve() {
        guardado("abc", ahora.minusSeconds(1), false);
        assertThatThrownBy(() -> service.restablecer("abc", "clave-nueva-123"))
                .isInstanceOf(AuthException.class).hasMessage(PasswordResetService.ENLACE_INVALIDO);
        verify(usuarios, never()).updatePassword(any(), any());
    }

    @Test
    void enlaceUsadoNoSirveDosVeces() {
        guardado("abc", ahora.plusMinutes(10), true);
        assertThatThrownBy(() -> service.restablecer("abc", "clave-nueva-123")).isInstanceOf(AuthException.class);
        verify(usuarios, never()).updatePassword(any(), any());
    }

    @Test
    void siOtraPeticionLoUsoPrimeroFalla() {
        guardado("abc", ahora.plusMinutes(10), false);
        when(resets.marcarUsado(1L, ahora)).thenReturn(0);
        assertThatThrownBy(() -> service.restablecer("abc", "clave-nueva-123")).isInstanceOf(AuthException.class);
        verify(usuarios, never()).updatePassword(any(), any());
    }

    @Test
    void tokenInventadoNoSirve() {
        assertThatThrownBy(() -> service.restablecer("inventado", "clave-nueva-123")).isInstanceOf(AuthException.class);
        assertThatThrownBy(() -> service.restablecer(" ", "clave-nueva-123")).isInstanceOf(AuthException.class);
    }
}
