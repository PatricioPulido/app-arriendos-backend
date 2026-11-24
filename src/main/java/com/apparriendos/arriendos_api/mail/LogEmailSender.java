package com.apparriendos.arriendos_api.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sin proveedor de correo configurado (desarrollo): el correo no se envia, se escribe
 * en el log del backend para poder copiar el enlace.
 */
public class LogEmailSender implements EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(LogEmailSender.class);

    @Override
    public void enviar(String para, String asunto, String html, String texto) {
        logger.warn("BREVO_API_KEY no configurada: el correo NO se envió. Para {} | {}\n{}", para, asunto, texto);
    }
}
