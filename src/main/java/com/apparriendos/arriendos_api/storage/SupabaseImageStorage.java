package com.apparriendos.arriendos_api.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Sube y borra fotos en Supabase Storage por su API REST.
 * La secret key (o la service_role antigua) solo vive en el backend: nunca va al front.
 */
public class SupabaseImageStorage implements ImageStorage {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseImageStorage.class);

    private final RestClient http;
    private final String baseUrl;
    private final String bucket;

    public SupabaseImageStorage(String supabaseUrl, String secretKey, String bucket) {
        this.baseUrl = supabaseUrl.replaceAll("/+$", "");
        this.bucket = bucket;
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.http = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(this.baseUrl + "/storage/v1")
                .defaultHeader("apikey", secretKey)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                .build();
    }

    @Override
    public String guardar(String ruta, byte[] datos, String contentType) {
        try {
            http.post()
                    .uri("/object/" + bucket + "/" + ruta)
                    .contentType(MediaType.parseMediaType(contentType))
                    // El nombre es unico (UUID), asi que el navegador puede guardarla en cache 1 año
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
                    .header("x-upsert", "false")
                    .body(datos)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            logger.error("No se pudo subir {} a Supabase Storage", ruta, e);
            throw new StorageException("No se pudo guardar la foto. Intenta de nuevo.", e);
        }
        return baseUrl + "/storage/v1/object/public/" + bucket + "/" + ruta;
    }

    @Override
    public void borrar(List<String> rutas) {
        if (rutas == null || rutas.isEmpty()) return;
        try {
            http.method(org.springframework.http.HttpMethod.DELETE)
                    .uri("/object/" + bucket)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("prefixes", rutas))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            // La fila ya se borro: queda un archivo huerfano, no un error para el usuario
            logger.warn("No se pudieron borrar {} archivos de Supabase Storage: {}", rutas.size(), e.getMessage());
        }
    }
}
