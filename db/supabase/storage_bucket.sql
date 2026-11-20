-- Solo en Supabase (no en Postgres local): crea el bucket publico de fotos.
-- Ejecutar una vez en el SQL Editor de Supabase.
-- Publico = cualquiera puede VER las fotos por su URL. Subir y borrar solo lo hace
-- el backend con la secret key; no se crean politicas para anon/authenticated.
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES ('departamentos', 'departamentos', true, 5242880,
        ARRAY['image/jpeg', 'image/png', 'image/webp'])
ON CONFLICT (id) DO NOTHING;
