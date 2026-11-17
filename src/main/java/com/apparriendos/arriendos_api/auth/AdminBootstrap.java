package com.apparriendos.arriendos_api.auth;

import com.apparriendos.arriendos_api.mapper.UsuarioMapper;
import com.apparriendos.arriendos_api.model.Rol;
import com.apparriendos.arriendos_api.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el primer ADMIN al arrancar si ADMIN_EMAIL y ADMIN_PASSWORD estan definidos
 * y todavia no existe ningun admin. Despues puedes quitar esas variables.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminBootstrap(UsuarioMapper usuarioMapper, PasswordEncoder passwordEncoder,
                          @Value("${app.admin.email:}") String email,
                          @Value("${app.admin.password:}") String password) {
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) return;
        if (password.length() < 12) {
            logger.warn("ADMIN_PASSWORD debe tener al menos 12 caracteres; no se creó el admin");
            return;
        }
        try {
            if (usuarioMapper.countByRol(Rol.ADMIN.name()) > 0) return;
            Usuario admin = new Usuario();
            admin.setEmail(email.trim().toLowerCase());
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setRol(Rol.ADMIN);
            admin.setActivo(true);
            usuarioMapper.insert(admin);
            logger.info("Usuario ADMIN inicial creado: {}", admin.getEmail());
        } catch (Exception ex) {
            logger.error("No se pudo crear el ADMIN inicial (¿ejecutaste db/migrations?)", ex);
        }
    }
}
