package com.apparriendos.arriendos_api.auth;

import com.apparriendos.arriendos_api.auth.AuthDtos.AuthResponse;
import com.apparriendos.arriendos_api.auth.AuthDtos.RegisterRequest;
import com.apparriendos.arriendos_api.auth.AuthDtos.Tokens;
import com.apparriendos.arriendos_api.auth.AuthDtos.UsuarioDto;
import com.apparriendos.arriendos_api.mapper.ArrendatarioMapper;
import com.apparriendos.arriendos_api.mapper.RefreshTokenMapper;
import com.apparriendos.arriendos_api.mapper.UsuarioMapper;
import com.apparriendos.arriendos_api.model.Arrendatario;
import com.apparriendos.arriendos_api.model.RefreshToken;
import com.apparriendos.arriendos_api.model.Rol;
import com.apparriendos.arriendos_api.model.Usuario;
import com.apparriendos.arriendos_api.security.JwtProperties;
import com.apparriendos.arriendos_api.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String CREDENCIALES_INVALIDAS = "Email o contraseña incorrectos";
    private static final SecureRandom RANDOM = new SecureRandom();
    /**
     * Si dos pestanas renuevan la sesion a la vez, la segunda llega con el token que la
     * primera acaba de rotar. Dentro de esta ventana no se toma como robo: se responde 409
     * y el front reintenta con la cookie nueva.
     */
    static final Duration GRACIA_ROTACION = Duration.ofSeconds(20);

    private final UsuarioMapper usuarioMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final ArrendatarioMapper arrendatarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties props;
    /** Hash de relleno para que un email inexistente tarde lo mismo que una contraseña mala. */
    private final String dummyHash;

    public AuthService(UsuarioMapper usuarioMapper, RefreshTokenMapper refreshTokenMapper,
                       ArrendatarioMapper arrendatarioMapper, PasswordEncoder passwordEncoder,
                       JwtService jwtService, JwtProperties props) {
        this.usuarioMapper = usuarioMapper;
        this.refreshTokenMapper = refreshTokenMapper;
        this.arrendatarioMapper = arrendatarioMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.props = props;
        this.dummyHash = passwordEncoder.encode("relleno-para-tiempo-constante");
    }

    /**
     * Registro publico. Toda cuenta tiene su ficha de arrendatario (nombre, telefono) para poder
     * reservar; el tipo de cuenta decide el rol: HUESPED -> ARRENDATARIO, ANFITRION -> ANFITRION.
     */
    @Transactional
    public Tokens register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (usuarioMapper.findByEmail(email) != null) {
            throw new AuthException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese email");
        }

        Arrendatario arr = new Arrendatario();
        arr.setNombre(req.nombre().trim());
        arr.setApellido(req.apellido().trim());
        arr.setEmail(email);
        arr.setTelefono(req.telefono());
        arrendatarioMapper.insert(arr);

        Usuario u = new Usuario();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRol(req.tipoCuenta() == null ? Rol.ARRENDATARIO : req.tipoCuenta().rol());
        u.setIdArrendatario(arr.getIdArrendatario());
        u.setActivo(true);
        usuarioMapper.insert(u);

        logger.info("Nuevo usuario registrado id={} rol={}", u.getIdUsuario(), u.getRol());
        return issueTokens(u);
    }

    @Transactional
    public Tokens login(String email, String password) {
        Usuario u = usuarioMapper.findByEmail(email.trim());
        if (u == null) {
            passwordEncoder.matches(password, dummyHash);
            throw new AuthException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }
        if (!passwordEncoder.matches(password, u.getPasswordHash()) || !u.isActivo()) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }
        return issueTokens(u);
    }

    /**
     * Cambia un refresh token valido por uno nuevo (rotacion). Si llega un token ya usado,
     * se asume robo y se revocan todas las sesiones de ese usuario.
     * noRollbackFor: la revocacion debe quedar guardada aunque se responda 401.
     */
    @Transactional(noRollbackFor = AuthException.class)
    public Tokens refresh(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Sesión expirada");
        }
        RefreshToken stored = refreshTokenMapper.findByHash(sha256(rawToken));
        if (stored == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Sesión expirada");
        }
        if (stored.isRevocado()) {
            if (stored.getFechaRevocacion() != null
                    && stored.getFechaRevocacion().isAfter(LocalDateTime.now().minus(GRACIA_ROTACION))) {
                throw new AuthException(HttpStatus.CONFLICT, "La sesión se renovó en otra pestaña");
            }
            logger.warn("Refresh token reutilizado para usuario id={}; se cierran todas sus sesiones", stored.getIdUsuario());
            refreshTokenMapper.revokeAllForUser(stored.getIdUsuario());
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Sesión expirada");
        }
        if (stored.getExpira().isBefore(LocalDateTime.now())) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Sesión expirada");
        }
        Usuario u = usuarioMapper.findById(stored.getIdUsuario());
        if (u == null || !u.isActivo()) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Sesión expirada");
        }
        if (refreshTokenMapper.revoke(stored.getIdRefreshToken()) == 0) {
            // Otra peticion lo roto entre la lectura y este UPDATE
            throw new AuthException(HttpStatus.CONFLICT, "La sesión se renovó en otra pestaña");
        }
        return issueTokens(u);
    }

    /**
     * Un huesped pasa a ser anfitrion (como "Hazte anfitrion" de Airbnb). Entrega tokens nuevos
     * con el rol actualizado y revoca el refresh token que traia.
     */
    @Transactional
    public Tokens hacermeAnfitrion(Long idUsuario, String rawRefresh) {
        Usuario u = usuarioMapper.findById(idUsuario);
        if (u == null || !u.isActivo()) throw new AuthException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado");
        if (u.getRol() == Rol.ARRENDATARIO) {
            usuarioMapper.updateRol(u.getIdUsuario(), Rol.ANFITRION.name());
            u.setRol(Rol.ANFITRION);
            logger.info("Usuario id={} ahora es ANFITRION", u.getIdUsuario());
        }
        logout(rawRefresh);
        return issueTokens(u);
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return;
        RefreshToken stored = refreshTokenMapper.findByHash(sha256(rawToken));
        if (stored != null && !stored.isRevocado()) {
            refreshTokenMapper.revoke(stored.getIdRefreshToken());
        }
    }

    public UsuarioDto me(Long idUsuario) {
        Usuario u = usuarioMapper.findById(idUsuario);
        if (u == null) throw new AuthException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado");
        return toDto(u);
    }

    private Tokens issueTokens(Usuario u) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken rt = new RefreshToken();
        rt.setIdUsuario(u.getIdUsuario());
        rt.setTokenHash(sha256(raw));
        rt.setExpira(LocalDateTime.now().plus(props.refreshTtl()));
        refreshTokenMapper.insert(rt);

        AuthResponse body = new AuthResponse(
                jwtService.createAccessToken(u), "Bearer", jwtService.accessTtlSeconds(), toDto(u));
        return new Tokens(body, raw);
    }

    private static UsuarioDto toDto(Usuario u) {
        return new UsuarioDto(u.getIdUsuario(), u.getEmail(), u.getRol(), u.getIdArrendatario());
    }

    static String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
