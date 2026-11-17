package com.apparriendos.arriendos_api.auth;

import com.apparriendos.arriendos_api.controller.ArrendatarioController;
import com.apparriendos.arriendos_api.controller.DepartamentoController;
import com.apparriendos.arriendos_api.controller.ReservaController;
import com.apparriendos.arriendos_api.model.Reserva;
import com.apparriendos.arriendos_api.model.Rol;
import com.apparriendos.arriendos_api.model.Usuario;
import com.apparriendos.arriendos_api.security.JwtConfig;
import com.apparriendos.arriendos_api.security.JwtService;
import com.apparriendos.arriendos_api.security.SecurityConfig;
import com.apparriendos.arriendos_api.service.ArrendatarioService;
import com.apparriendos.arriendos_api.service.DepartamentoService;
import com.apparriendos.arriendos_api.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {DepartamentoController.class, ArrendatarioController.class,
        ReservaController.class, AuthController.class})
@Import({SecurityConfig.class, JwtConfig.class, JwtService.class, AuthExceptionHandler.class})
@TestPropertySource(properties = "app.jwt.secret=clave-de-prueba-de-al-menos-32-caracteres!!")
class SecurityRulesTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;

    @MockitoBean DepartamentoService departamentoService;
    @MockitoBean ArrendatarioService arrendatarioService;
    @MockitoBean ReservaService reservaService;
    @MockitoBean AuthService authService;

    private String adminToken;
    private String arrendatarioToken;

    @BeforeEach
    void setUp() {
        doReturn(ResponseEntity.ok(List.of())).when(departamentoService).getAll();
        doReturn(ResponseEntity.status(201).build()).when(departamentoService).create(any());
        doReturn(ResponseEntity.ok(List.of())).when(arrendatarioService).getAll();
        doReturn(ResponseEntity.status(201).build()).when(reservaService).create(any());
        doReturn(ResponseEntity.ok(List.of())).when(reservaService).getAll();
        doReturn(ResponseEntity.ok(List.of())).when(reservaService).getByArrendatario(any());

        adminToken = token(1L, Rol.ADMIN, null);
        arrendatarioToken = token(2L, Rol.ARRENDATARIO, 50L);
    }

    private String token(Long id, Rol rol, Long idArrendatario) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setEmail("u" + id + "@test.cl");
        u.setRol(rol);
        u.setIdArrendatario(idArrendatario);
        return "Bearer " + jwtService.createAccessToken(u);
    }

    private static final String DEPTO = "{\"nombre\":\"Depto 1\",\"numero\":101}";

    @Test
    void catalogoEsPublico() throws Exception {
        mvc.perform(get("/api/departamentos")).andExpect(status().isOk());
    }

    @Test
    void anonimoNoPuedeCrearDepartamento() throws Exception {
        mvc.perform(post("/api/departamentos").contentType(MediaType.APPLICATION_JSON).content(DEPTO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").exists());
        verify(departamentoService, never()).create(any());
    }

    @Test
    void arrendatarioNoPuedeCrearDepartamento() throws Exception {
        mvc.perform(post("/api/departamentos").header("Authorization", arrendatarioToken)
                        .contentType(MediaType.APPLICATION_JSON).content(DEPTO))
                .andExpect(status().isForbidden());
        verify(departamentoService, never()).create(any());
    }

    @Test
    void adminPuedeCrearDepartamento() throws Exception {
        mvc.perform(post("/api/departamentos").header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(DEPTO))
                .andExpect(status().isCreated());
    }

    @Test
    void tokenFalsoEsRechazado() throws Exception {
        mvc.perform(get("/api/arrendatarios").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void datosPersonalesSoloParaAdmin() throws Exception {
        mvc.perform(get("/api/arrendatarios")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/arrendatarios").header("Authorization", arrendatarioToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/arrendatarios").header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void arrendatarioNoVeTodasLasReservas() throws Exception {
        mvc.perform(get("/api/reservas").header("Authorization", arrendatarioToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/reservas/mias").header("Authorization", arrendatarioToken))
                .andExpect(status().isOk());
        verify(reservaService).getByArrendatario(50L);
    }

    @Test
    void arrendatarioSoloReservaASuNombre() throws Exception {
        String body = "{\"idDepartamento\":1,\"idArrendatario\":999,\"fechaInicio\":\"2026-12-01\","
                + "\"fechaFin\":\"2026-12-05\",\"estado\":\"CONFIRMADA\"}";
        mvc.perform(post("/api/reservas").header("Authorization", arrendatarioToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        ArgumentCaptor<Reserva> captor = ArgumentCaptor.forClass(Reserva.class);
        verify(reservaService).create(captor.capture());
        assertThat(captor.getValue().getIdArrendatario()).isEqualTo(50L);
        assertThat(captor.getValue().getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    void loginEsPublicoYEntregaCookieHttpOnly() throws Exception {
        var dto = new AuthDtos.UsuarioDto(2L, "u2@test.cl", Rol.ARRENDATARIO, 50L);
        when(authService.login("u2@test.cl", "clave-segura"))
                .thenReturn(new AuthDtos.Tokens(new AuthDtos.AuthResponse("tok", "Bearer", 900, dto), "refresh123"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"u2@test.cl\",\"password\":\"clave-segura\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("tok"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("refresh_token=refresh123"),
                        org.hamcrest.Matchers.containsString("HttpOnly"),
                        org.hamcrest.Matchers.containsString("Path=/api/auth"))));
    }

    @Test
    void loginConCredencialesMalasDa401() throws Exception {
        when(authService.login(any(), any()))
                .thenThrow(new AuthException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@test.cl\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Email o contraseña incorrectos"));
    }

    @Test
    void meRequiereToken() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }
}
