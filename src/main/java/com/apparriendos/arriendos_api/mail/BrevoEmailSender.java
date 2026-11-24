package com.apparriendos.arriendos_api.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Envia correos con la API HTTP de Brevo (https://developers.brevo.com/reference/sendtransacemail).
 * Se usa HTTP y no SMTP porque Render bloquea los puertos SMTP en el plan gratis.
 * Brevo deja enviar a cualquier destinatario con solo verificar el correo remitente
 * (no hace falta tener dominio propio).
 */
public class BrevoEmailSender implements EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(BrevoEmailSender.class);

    private final RestClient http;
    private final String remitente;
    private final String nombreRemitente;

    public BrevoEmailSender(String apiKey, String remitente, String nombreRemitente) {
        this.remitente = remitente;
        this.nombreRemitente = nombreRemitente;
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofSeconds(20));
        this.http = RestClient.builder()
                .requestFactory(factory)
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .build();
    }

    @Override
    public void enviar(String para, String asunto, String html, String texto) {
        try {
            http.post()
                    .uri("/smtp/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "sender", Map.of("email", remitente, "name", nombreRemitente),
                            "to", List.of(Map.of("email", para)),
                            "subject", asunto,
                            "htmlContent", html,
                            "textContent", texto))
                    .retrieve()
                    .toBodilessEntity();
            logger.info("Correo enviado: {}", asunto);
        } catch (RestClientException e) {
            // No se muestra al usuario: la respuesta de "revisa tu correo" es siempre la misma
            logger.error("Brevo no pudo enviar el correo '{}': {}", asunto, e.getMessage());
        }
    }
}
