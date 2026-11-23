package com.apparriendos.arriendos_api.auth;

import com.apparriendos.arriendos_api.mapper.ArrendatarioMapper;
import com.apparriendos.arriendos_api.mapper.RefreshTokenMapper;
import com.apparriendos.arriendos_api.mapper.UsuarioMapper;
import com.apparriendos.arriendos_api.model.RefreshToken;
import com.apparriendos.arriendos_api.model.Rol;
import com.apparriendos.arriendos_api.model.Usuario;
import com.apparriendos.arriendos_api.security.JwtProperties;
import com.apparriendos.arriendos_api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    UsuarioMapper usuarios = mock(UsuarioMapper.class);
    RefreshTokenMapper tokens = mock(RefreshTokenMapper.class);
    ArrendatarioMapper arrendatarios = mock(ArrendatarioMapper.class);
    JwtService jwt = mock(JwtService.class);
    PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    AuthService service;
    Usuario usuario;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties("clave-de-prueba-de-al-menos-32-caracteres!!", null,
                Duration.ofMinutes(15), Duration.ofDays(7));
        service = new AuthService(usuarios, tokens, arrendatarios, encoder, jwt, props);
        when(jwt.createAccessToken(any())).thenReturn("access");

        usuario = new Usuario();
        usuario.setIdUsuario(7L);
        usuario.setEmail("ana@test.cl");
        usuario.setPasswordHash(encoder.encode("clave-correcta"));
        usuario.setRol(Rol.ARRENDATARIO);
        usuario.setActivo(true);
    }

    @Test
    void loginCorrectoEntregaTokens() {
        when(usuarios.findByEmail("ana@test.cl")).thenReturn(usuario);
        var result = service.login("ana@test.cl", "clave-correcta");
        assertThat(result.body().accessToken()).isEqualTo("access");
        assertThat(result.refreshToken()).hasSizeGreaterThan(30);
        // en la BD se guarda el hash, no el token
        verify(tokens).insert(argThat(t -> t.getTokenHash().equals(AuthService.sha256(result.refreshToken()))));
    }

    @Test
    void loginConClaveMalaFalla() {
        when(usuarios.findByEmail("ana@test.cl")).thenReturn(usuario);
        assertThatThrownBy(() -> service.login("ana@test.cl", "otra"))
                .isInstanceOf(AuthException.class).hasMessage("Email o contraseña incorrectos");
    }

    @Test
    void loginConEmailInexistenteDaElMismoMensaje() {
        assertThatThrownBy(() -> service.login("nadie@test.cl", "x"))
                .isInstanceOf(AuthException.class).hasMessage("Email o contraseña incorrectos");
    }

    @Test
    void usuarioInactivoNoEntra() {
        usuario.setActivo(false);
        when(usuarios.findByEmail("ana@test.cl")).thenReturn(usuario);
        assertThatThrownBy(() -> service.login("ana@test.cl", "clave-correcta")).isInstanceOf(AuthException.class);
    }

    @Test
    void refreshRotaElToken() {
        RefreshToken rt = new RefreshToken();
        rt.setIdRefreshToken(3L);
        rt.setIdUsuario(7L);
        rt.setExpira(LocalDateTime.now().plusDays(1));
        when(tokens.findByHash(AuthService.sha256("viejo"))).thenReturn(rt);
        when(usuarios.findById(7L)).thenReturn(usuario);
        when(tokens.revoke(3L)).thenReturn(1);

        var result = service.refresh("viejo");

        verify(tokens).revoke(3L);
        assertThat(result.refreshToken()).isNotEqualTo("viejo");
    }

    @Test
    void refreshReutilizadoCierraTodasLasSesiones() {
        RefreshToken rt = new RefreshToken();
        rt.setIdRefreshToken(3L);
        rt.setIdUsuario(7L);
        rt.setRevocado(true);
        rt.setFechaRevocacion(LocalDateTime.now().minusHours(1));
        rt.setExpira(LocalDateTime.now().plusDays(1));
        when(tokens.findByHash(AuthService.sha256("robado"))).thenReturn(rt);

        assertThatThrownBy(() -> service.refresh("robado")).isInstanceOf(AuthException.class);
        verify(tokens).revokeAllForUser(7L);
    }

    @Test
    void refreshExpiradoFalla() {
        RefreshToken rt = new RefreshToken();
        rt.setIdRefreshToken(3L);
        rt.setIdUsuario(7L);
        rt.setExpira(LocalDateTime.now().minusMinutes(1));
        when(tokens.findByHash(AuthService.sha256("viejo"))).thenReturn(rt);
        assertThatThrownBy(() -> service.refresh("viejo")).isInstanceOf(AuthException.class);
    }

    @Test
    void registroCreaArrendatarioYUsuario() {
        doAnswer(inv -> { inv.<com.apparriendos.arriendos_api.model.Arrendatario>getArgument(0).setIdArrendatario(50L); return null; })
                .when(arrendatarios).insert(any());
        var req = new AuthDtos.RegisterRequest("Ana", "Pérez", "Ana@Test.cl", null, "clave-segura-1", null);
        service.register(req);
        verify(usuarios).insert(argThat(u -> u.getRol() == Rol.ARRENDATARIO
                && u.getIdArrendatario() == 50L
                && u.getEmail().equals("ana@test.cl")
                && !u.getPasswordHash().equals("clave-segura-1")));
    }

    @Test
    void registroConEmailRepetidoFalla() {
        when(usuarios.findByEmail("ana@test.cl")).thenReturn(usuario);
        var req = new AuthDtos.RegisterRequest("Ana", "Pérez", "ana@test.cl", null, "clave-segura-1", null);
        assertThatThrownBy(() -> service.register(req)).isInstanceOf(AuthException.class);
        verify(arrendatarios, never()).insert(any());
    }

    @Test
    void refreshDeOtraPestanaRecienRotadoNoCierraSesiones() {
        RefreshToken rt = new RefreshToken();
        rt.setIdRefreshToken(3L);
        rt.setIdUsuario(7L);
        rt.setRevocado(true);
        rt.setFechaRevocacion(LocalDateTime.now().minusSeconds(2));
        rt.setExpira(LocalDateTime.now().plusDays(1));
        when(tokens.findByHash(AuthService.sha256("recien"))).thenReturn(rt);

        assertThatThrownBy(() -> service.refresh("recien"))
                .isInstanceOfSatisfying(AuthException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(org.springframework.http.HttpStatus.CONFLICT));
        verify(tokens, never()).revokeAllForUser(any());
    }

    @Test
    void refreshQuePierdeLaCarreraDa409() {
        RefreshToken rt = new RefreshToken();
        rt.setIdRefreshToken(3L);
        rt.setIdUsuario(7L);
        rt.setExpira(LocalDateTime.now().plusDays(1));
        when(tokens.findByHash(AuthService.sha256("viejo"))).thenReturn(rt);
        when(usuarios.findById(7L)).thenReturn(usuario);
        when(tokens.revoke(3L)).thenReturn(0);

        assertThatThrownBy(() -> service.refresh("viejo")).isInstanceOf(AuthException.class);
        verify(tokens, never()).insert(any());
    }

    @Test
    void registroComoAnfitrion() {
        var req = new AuthDtos.RegisterRequest("Ana", "Pérez", "ana@test.cl", null, "clave-segura-1",
                com.apparriendos.arriendos_api.model.TipoCuenta.ANFITRION);
        service.register(req);
        verify(arrendatarios).insert(any()); // tambien puede reservar
        verify(usuarios).insert(argThat(u -> u.getRol() == Rol.ANFITRION));
    }

    @Test
    void huespedSeHaceAnfitrion() {
        when(usuarios.findById(7L)).thenReturn(usuario);
        var result = service.hacermeAnfitrion(7L, null);
        verify(usuarios).updateRol(7L, "ANFITRION");
        assertThat(result.body().usuario().rol()).isEqualTo(Rol.ANFITRION);
    }
}
