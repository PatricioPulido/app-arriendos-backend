package com.apparriendos.arriendos_api.auth;

import com.apparriendos.arriendos_api.mail.EmailSender;
import com.apparriendos.arriendos_api.mapper.PasswordResetMapper;
import com.apparriendos.arriendos_api.mapper.RefreshTokenMapper;
import com.apparriendos.arriendos_api.mapper.UsuarioMapper;
import com.apparriendos.arriendos_api.model.PasswordResetToken;
import com.apparriendos.arriendos_api.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * "¿Olvidaste tu contraseña?":
 * 1) solicitar(email): si la cuenta existe, crea un token al azar (se guarda solo su hash)
 *    y envia por correo un enlace al front con ese token. Responde siempre igual.
 * 2) restablecer(token, clave): si el token existe, no vencio y no se uso, cambia la
 *    clave, lo marca usado, anula los demas enlaces y cierra todas las sesiones abiertas.
 */
@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    static final Duration VIGENCIA = Duration.ofMinutes(30);
    /** Maximo de correos de recuperacion por cuenta cada hora. */
    static final int MAX_POR_HORA = 3;
    static final String ENLACE_INVALIDO =
            "El enlace no es válido o ya venció. Pide uno nuevo desde \"¿Olvidaste tu contraseña?\".";

    private final UsuarioMapper usuarioMapper;
    private final PasswordResetMapper resetMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final Executor correoExecutor;
    private final String frontUrl;
    private final String marca;
    private final Clock clock;

    @Autowired
    public PasswordResetService(UsuarioMapper usuarioMapper, PasswordResetMapper resetMapper,
                                RefreshTokenMapper refreshTokenMapper, PasswordEncoder passwordEncoder,
                                EmailSender emailSender,
                                @Qualifier("correoExecutor") Executor correoExecutor,
                                @Value("${app.mail.reset-url-base:${app.cors.front-url:https://arriendos-tau.vercel.app}}") String frontUrl,
                                @Value("${app.mail.from-name:ArriendoChile}") String marca) {
        this(usuarioMapper, resetMapper, refreshTokenMapper, passwordEncoder, emailSender,
                correoExecutor, frontUrl, marca, Clock.systemUTC());
    }

    PasswordResetService(UsuarioMapper usuarioMapper, PasswordResetMapper resetMapper,
                         RefreshTokenMapper refreshTokenMapper, PasswordEncoder passwordEncoder,
                         EmailSender emailSender, Executor correoExecutor,
                         String frontUrl, String marca, Clock clock) {
        this.usuarioMapper = usuarioMapper;
        this.resetMapper = resetMapper;
        this.refreshTokenMapper = refreshTokenMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.correoExecutor = correoExecutor;
        this.frontUrl = frontUrl.replaceAll("/+$", "");
        this.marca = marca;
        this.clock = clock;
    }

    public void solicitar(String emailIngresado) {
        String email = emailIngresado.trim().toLowerCase();
        Usuario u = usuarioMapper.findByEmail(email);
        if (u == null || !u.isActivo()) {
            logger.info("Recuperación pedida para un email sin cuenta activa");
            return;
        }
        LocalDateTime ahora = ahora();
        if (resetMapper.countDesde(u.getIdUsuario(), ahora.minusHours(1)) >= MAX_POR_HORA) {
            logger.info("Usuario id={} ya pidió {} enlaces en la última hora; no se envía otro", u.getIdUsuario(), MAX_POR_HORA);
            return;
        }

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        PasswordResetToken t = new PasswordResetToken();
        t.setIdUsuario(u.getIdUsuario());
        t.setTokenHash(AuthService.sha256(token));
        t.setExpira(ahora.plus(VIGENCIA));
        t.setFechaCreacion(ahora);
        resetMapper.insert(t);

        String enlace = frontUrl + "/restablecer-contrasena?token=" + token;
        String destino = u.getEmail();
        logger.info("Enlace de recuperación creado para usuario id={}", u.getIdUsuario());
        try {
            correoExecutor.execute(() -> emailSender.enviar(destino, asunto(), html(enlace), texto(enlace)));
        } catch (RejectedExecutionException e) {
            logger.error("No se pudo encolar el correo de recuperación", e);
        }
    }

    @Transactional
    public void restablecer(String token, String nuevaClave) {
        if (token == null || token.isBlank()) throw invalido();
        PasswordResetToken t = resetMapper.findByHash(AuthService.sha256(token.trim()));
        LocalDateTime ahora = ahora();
        if (t == null || t.isUsado() || t.getExpira().isBefore(ahora)) throw invalido();

        Usuario u = usuarioMapper.findById(t.getIdUsuario());
        if (u == null || !u.isActivo()) throw invalido();
        // UPDATE ... WHERE USADO = 0: si dos peticiones llegan a la vez, solo una gana
        if (resetMapper.marcarUsado(t.getIdReset(), ahora) == 0) throw invalido();

        usuarioMapper.updatePassword(u.getIdUsuario(), passwordEncoder.encode(nuevaClave));
        resetMapper.anularPendientes(u.getIdUsuario(), ahora);
        // Quien tenia la clave vieja (o una sesion robada) queda afuera
        refreshTokenMapper.revokeAllForUser(u.getIdUsuario());
        logger.info("Contraseña restablecida para usuario id={}", u.getIdUsuario());
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(clock);
    }

    private static AuthException invalido() {
        return new AuthException(HttpStatus.BAD_REQUEST, ENLACE_INVALIDO);
    }

    private String asunto() {
        return "Restablece tu contraseña de " + marca;
    }

    private String texto(String enlace) {
        return "Hola,\n\nRecibimos una solicitud para cambiar la contraseña de tu cuenta en " + marca + ".\n"
                + "Abre este enlace para elegir una nueva (vence en 30 minutos y sirve una sola vez):\n\n"
                + enlace + "\n\nSi no fuiste tú, ignora este correo: tu contraseña no cambia.\n";
    }

    private String html(String enlace) {
        String e = HtmlUtils.htmlEscape(enlace);
        String m = HtmlUtils.htmlEscape(marca);
        return """
                <div style="font-family:Arial,Helvetica,sans-serif;max-width:480px;margin:auto;color:#0f172a">
                  <h2 style="color:#0e7490">%s</h2>
                  <p>Hola,</p>
                  <p>Recibimos una solicitud para cambiar la contraseña de tu cuenta.</p>
                  <p style="margin:28px 0">
                    <a href="%s" style="background:#0e7490;color:#fff;padding:12px 22px;border-radius:10px;text-decoration:none;font-weight:bold">Elegir nueva contraseña</a>
                  </p>
                  <p style="color:#475569;font-size:14px">El enlace vence en 30 minutos y sirve una sola vez.
                  Si el botón no funciona, copia esta dirección en el navegador:<br><span style="word-break:break-all">%s</span></p>
                  <p style="color:#475569;font-size:14px">Si no fuiste tú, ignora este correo: tu contraseña no cambia.</p>
                </div>
                """.formatted(m, e, e);
    }
}
