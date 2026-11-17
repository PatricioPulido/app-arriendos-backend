package com.apparriendos.arriendos_api.auth;

import com.apparriendos.arriendos_api.auth.AuthDtos.AuthResponse;
import com.apparriendos.arriendos_api.auth.AuthDtos.LoginRequest;
import com.apparriendos.arriendos_api.auth.AuthDtos.RegisterRequest;
import com.apparriendos.arriendos_api.auth.AuthDtos.Tokens;
import com.apparriendos.arriendos_api.auth.AuthDtos.UsuarioDto;
import com.apparriendos.arriendos_api.security.JwtProperties;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    static final String REFRESH_COOKIE = "refresh_token";

    private final AuthService authService;
    private final JwtProperties props;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public AuthController(AuthService authService, JwtProperties props,
                          @Value("${app.auth.cookie-secure:true}") boolean cookieSecure,
                          @Value("${app.auth.cookie-same-site:Strict}") String cookieSameSite) {
        this.authService = authService;
        this.props = props;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return withCookie(HttpStatus.CREATED, authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return withCookie(HttpStatus.OK, authService.login(req.email(), req.password()));
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
