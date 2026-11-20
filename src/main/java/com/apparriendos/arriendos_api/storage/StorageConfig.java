package com.apparriendos.arriendos_api.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Elige donde guardar fotos: si SUPABASE_URL y SUPABASE_SECRET_KEY estan definidas usa
 * Supabase Storage; si no, una carpeta local servida en /uploads/** (solo para desarrollo).
 */
@Configuration
public class StorageConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(StorageConfig.class);

    @Value("${app.storage.supabase-url:}")
    private String supabaseUrl;
    @Value("${app.storage.supabase-secret-key:}")
    private String supabaseKey;
    @Value("${app.storage.bucket:departamentos}")
    private String bucket;
    @Value("${app.storage.local-dir:uploads}")
    private String localDir;
    @Value("${app.storage.local-url:http://localhost:8080/uploads}")
    private String localUrl;

    @Bean
    public ImageStorage imageStorage() {
        if (usaSupabase()) {
            logger.info("Fotos en Supabase Storage, bucket '{}'", bucket);
            return new SupabaseImageStorage(supabaseUrl, supabaseKey, bucket);
        }
        LocalImageStorage local = new LocalImageStorage(localDir, localUrl);
        logger.warn("Fotos en carpeta local {} (define SUPABASE_URL y SUPABASE_SECRET_KEY para usar Supabase)", local.getRaiz());
        return local;
    }

    private boolean usaSupabase() {
        return !supabaseUrl.isBlank() && !supabaseKey.isBlank();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!usaSupabase()) {
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations(java.nio.file.Path.of(localDir).toAbsolutePath().normalize().toUri().toString());
        }
    }
}
