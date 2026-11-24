# Subir la app a internet (Supabase + Render + Vercel)

Arquitectura: el front en Vercel reenvia `/api/*` al backend en Render (ver
`vercel.json` del repo del front). Para el navegador todo es el mismo dominio:
la cookie del refresh funciona con `SameSite=Strict` y no hace falta CORS.
El backend usa la BD Postgres de Supabase.

## 1. Base de datos en Supabase

1. Crea cuenta en https://supabase.com con tu GitHub y un proyecto nuevo.
   Region: **South America (Sao Paulo)**, la mas cercana a Chile. Guarda la
   contrasena de la BD en un lugar seguro.
2. En el proyecto: **SQL Editor** > New query > pega y ejecuta (Run), en este orden:
   - `db/migrations/001_esquema_inicial.sql`
   - `db/migrations/002_anfitriones_y_galeria.sql`
   - `db/migrations/003_ciudades_turisticas.sql` (ciudades y zonas iniciales)
   - `db/migrations/004_ficha_alojamiento.sql` (comodidades, destacados, reglas y horarios de la ficha)
   - `db/supabase/storage_bucket.sql` (crea el bucket publico `departamentos` para las fotos)

   Cada migracion nueva (`003_...`) se ejecuta igual, una vez, en orden.
3. **Connect** (arriba) > **Session pooler**. Te muestra algo como
   `postgresql://postgres.abcd1234:[PASSWORD]@aws-0-sa-east-1.pooler.supabase.com:5432/postgres`.
   De ahi sacas las variables para Render:
   - `DB_URL=jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:5432/postgres?sslmode=require`
   - `DB_USERNAME=postgres.abcd1234`
   - `DB_PASSWORD=` la contrasena de la BD
   
   Usa el **Session pooler** (puerto 5432), no la conexion directa (solo IPv6,
   Render no llega) ni el Transaction pooler (6543, rompe los prepared
   statements de Java).

4. Fotos: **Project Settings > API Keys**. Copia la **Project URL**
   (`https://abcd1234.supabase.co`) y crea/copia una **secret key** (`sb_secret_...`).
   Para Render:
   - `SUPABASE_URL=https://abcd1234.supabase.co`
   - `SUPABASE_SECRET_KEY=sb_secret_...`

   La secret key salta las reglas de seguridad de Supabase: va SOLO en el backend,
   nunca en el front ni en git. El front sube fotos al backend, y el backend valida
   que sea el anfitrion de la propiedad y que el archivo sea JPG, PNG o WebP (max 5 MB).

Plan free: 500 MB de BD y 1 GB de archivos (para la galeria). El proyecto se
pausa si pasa 7 dias sin uso; se reactiva desde el panel.

## 2. Backend en Render

1. Crea cuenta en https://render.com con tu GitHub.
2. New > Blueprint > elige `app-arriendos-backend` (lee `render.yaml`).
3. Completa `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (paso 1), `ADMIN_EMAIL` y
   `ADMIN_PASSWORD`. `JWT_SECRET` se genera solo.
4. Espera el build (5-8 min la primera vez). Abre
   `https://<tu-servicio>.onrender.com/actuator/health`: debe decir `UP`.
5. Entra con el admin desde el front y luego BORRA `ADMIN_EMAIL` y
   `ADMIN_PASSWORD` de Render.

Plan free: se duerme tras 15 min sin uso y tarda ~1 min en despertar.
Cuando tengas clientes, pasa a Starter (USD 7/mes).

## 3. Frontend en Vercel

1. En `vercel.json` del front, cambia `CAMBIAR-POR-TU-BACKEND.onrender.com`
   por la URL real de Render y haz commit.
2. En https://vercel.com: Add New > Project > `app-arriendos-frontend`.
   Framework: Create React App. NO definas `REACT_APP_API_URL`.
3. Deploy. Prueba login, registro y el panel admin.

## Probar la imagen Docker en tu PC

    docker compose --profile app up -d --build
