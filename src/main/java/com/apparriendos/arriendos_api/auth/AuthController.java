package com.apparriendos.arriendos_api.auth;

import com.apparriendos.arriendos_api.auth.AuthDtos.AuthResponse;
import com.apparriendos.arriendos_api.auth.AuthDtos.LoginRequest;
import com.apparriendos.arriendos_api.auth.AuthDtos.RecuperarRequest;
import com.apparriendos.arriendos_api.auth.AuthDtos.RestablecerRequest;
import com.apparriendos.arriendos_api.auth.AuthDtos.RegisterRequest;
import com.apparriendos.arriendos_api.auth.AuthDtos.Tokens;
import com.apparriendos.arriendos_api.auth.AuthDtos.UsuarioDto;
import com.apparriendos.arriendos_api.security.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    static final String REFRESH_COOKIE = "refresh_token";

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final LoginRateLimiter rateLimiter;
    private final JwtProperties props;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public AuthController(AuthService authService, PasswordResetService passwordResetService,
                          LoginRateLimiter rateLimiter, JwtProperties props,
                          @Value("${app.auth.cookie-secure:true}") boolean cookieSecure,
                          @Value("${app.auth.cookie-same-site:Strict}") String cookieSameSite) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.rateLimiter = rateLimiter;
        this.props = props;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return withCookie(HttpStatus.CREATED, authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        // Con server.forward-headers-strategy=framework, getRemoteAddr ya es la IP real tras el proxy
        String ip = http.getRemoteAddr();
        rateLimiter.verificar(ip, req.email());
        try {
            Tokens tokens = authService.login(req.email(), req.password());
            rateLimiter.registrarExito(ip, req.email());
            return withCookie(HttpStatus.OK, tokens);
        } catch (AuthException e) {
            if (e.getStatus() == HttpStatus.UNAUTHORIZED) rateLimiter.registrarFallo(ip, req.email());
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = REFRESH_COOKIE, required = false) String token) {
        return withCookie(HttpStatus.OK, authService.refresh(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String token) {
        authService.logout(token);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString())
                .build();
    }

    /**
     * "¿Olvidaste tu contraseña?". Responde siempre lo mismo, exista o no la cuenta,
     * para que nadie pueda averiguar que emails estan registrados.
     */
    @PostMapping("/recuperar")
    public Map<String, String> recuperar(@Valid @RequestBody RecuperarRequest req, HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        // Mismo limite que el login (por IP y por email) para que no se use para mandar spam
        String clave = "recuperar|" + req.email();
        rateLimiter.verificar(ip, clave);
        rateLimiter.registrarFallo(ip, clave);
        passwordResetService.solicitar(req.email());
        return Map.of("mensaje", "Si existe una cuenta con ese email, te enviamos un enlace para crear una nueva contraseña. Revisa también la carpeta de spam.");
    }

    /** Cambia la contraseña con el token del enlace. Despues hay que iniciar sesion de nuevo. */
    @PostMapping("/restablecer")
    public Map<String, String> restablecer(@Valid @RequestBody RestablecerRequest req) {
        passwordResetService.restablecer(req.token(), req.password());
        return Map.of("mensaje", "Listo, tu contraseña se cambió. Ya puedes iniciar sesión.");
    }

    /** Un huesped activa su cuenta de anfitrion para publicar alojamientos. */
    @PostMapping("/hacerme-anfitrion")
    public ResponseEntity<AuthResponse> hacermeAnfitrion(@AuthenticationPrincipal Jwt jwt,
            @CookieValue(name = REFRESH_COOKIE, required = false) String token) {
        return withCookie(HttpStatus.OK, authService.hacermeAnfitrion(Long.valueOf(jwt.getSubject()), token));
    }

    @GetMapping("/me")
    public UsuarioDto me(@AuthenticationPrincipal Jwt jwt) {
        return authService.me(Long.valueOf(jwt.getSubject()));
    }

    private ResponseEntity<AuthResponse> withCookie(HttpStatus status, Tokens tokens) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie(tokens.refreshToken(), props.refreshTtl()).toString())
                .body(tokens.body());
    }

    /** Cookie HttpOnly: el JavaScript del front no puede leerla, solo el navegador la envia a /api/auth. */
    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
    }
}
