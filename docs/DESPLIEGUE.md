# Subir la API a internet (Render + Vercel)

Arquitectura: el front en Vercel reenvia `/api/*` al backend (ver `vercel.json`
del repo del front). Para el navegador todo es el mismo dominio: la cookie del
refresh funciona con `SameSite=Strict` y no hace falta CORS.

## 1. Base de datos

Necesitas una BD accesible desde internet y aplicar `db/migrations` en orden
(000, 001, 002). Ver la decision Oracle vs Postgres en el informe del proyecto.

## 2. Backend en Render

1. Crea cuenta en https://render.com con tu GitHub.
2. New > Blueprint > elige `app-arriendos-backend` (lee `render.yaml`).
3. Completa las variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_SCHEMA`,
   `ADMIN_EMAIL`, `ADMIN_PASSWORD`. `JWT_SECRET` se genera solo.
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

    docker build -t arriendos-api .
    docker run --env-file .env -e DB_URL=jdbc:oracle:thin:@//host.docker.internal:1521/XEPDB1 -p 8080:8080 arriendos-api

(Dentro del contenedor `localhost` es el propio contenedor; por eso se usa
`host.docker.internal` para llegar a la BD de tu PC.)
