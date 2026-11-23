package com.apparriendos.arriendos_api.auth;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Frena intentos de adivinar contrasenas: maximo 5 fallos por email+IP y 20 por IP
 * cada 15 minutos. En memoria: alcanza para una sola instancia del backend (Render).
 */
@Component
public class LoginRateLimiter {

    static final int MAX_POR_CUENTA = 5;
    static final int MAX_POR_IP = 20;
    static final Duration VENTANA = Duration.ofMinutes(15);

    private final Map<String, Deque<Instant>> fallos = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginRateLimiter() {
        this(Clock.systemUTC());
    }

    LoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    /** Lanza 429 si esa cuenta o esa IP ya fallo demasiadas veces. */
    public void verificar(String ip, String email) {
        if (contar(claveCuenta(ip, email)) >= MAX_POR_CUENTA || contar(claveIp(ip)) >= MAX_POR_IP) {
            throw new AuthException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos. Espera unos minutos e intenta de nuevo.");
        }
    }

    public void registrarFallo(String ip, String email) {
        Instant ahora = clock.instant();
        for (String k : new String[]{claveCuenta(ip, email), claveIp(ip)}) {
            fallos.compute(k, (key, q) -> {
                Deque<Instant> d = q == null ? new ArrayDeque<>() : q;
                d.addLast(ahora);
                return d;
            });
        }
        if (fallos.size() > 10_000) limpiar();
    }

    public void registrarExito(String ip, String email) {
        fallos.remove(claveCuenta(ip, email));
    }

    private int contar(String clave) {
        Instant limite = clock.instant().minus(VENTANA);
        Deque<Instant> d = fallos.computeIfPresent(clave, (k, q) -> {
            while (!q.isEmpty() && q.peekFirst().isBefore(limite)) q.pollFirst();
            return q.isEmpty() ? null : q;
        });
        return d == null ? 0 : d.size();
    }

    private void limpiar() {
        fallos.keySet().forEach(this::contar);
    }

    private static String claveCuenta(String ip, String email) {
        return "c|" + ip + "|" + (email == null ? "" : email.trim().toLowerCase());
    }

    private static String claveIp(String ip) {
        return "i|" + ip;
    }
}
