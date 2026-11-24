package com.apparriendos.arriendos_api.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
public class MailConfig {

    private static final Logger logger = LoggerFactory.getLogger(MailConfig.class);

    @Bean
    public EmailSender emailSender(@Value("${app.mail.brevo-api-key:}") String apiKey,
                                   @Value("${app.mail.from:}") String from,
                                   @Value("${app.mail.from-name:ArriendoChile}") String fromName) {
        if (apiKey.isBlank() || from.isBlank()) {
            logger.warn("Correo sin configurar (BREVO_API_KEY / MAIL_FROM): los enlaces de recuperación solo se verán en el log");
            return new LogEmailSender();
        }
        return new BrevoEmailSender(apiKey.trim(), from.trim(), fromName);
    }

    /**
     * Los correos salen en segundo plano: asi la respuesta tarda lo mismo exista o no
     * la cuenta, y no se puede averiguar que emails estan registrados.
     */
    @Bean(destroyMethod = "shutdown")
    public java.util.concurrent.ExecutorService correoExecutor() {
        return Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "correo");
            t.setDaemon(true);
            return t;
        });
    }
}
