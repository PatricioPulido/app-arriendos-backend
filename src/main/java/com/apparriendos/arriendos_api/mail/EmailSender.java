package com.apparriendos.arriendos_api.mail;

/** Envia correos de la app (por ahora, el enlace para recuperar la contraseña). */
public interface EmailSender {
    void enviar(String para, String asunto, String html, String texto);
}
