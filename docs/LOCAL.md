# Levantar todo en tu PC

La app usa PostgreSQL: Supabase en produccion y un Postgres en Docker en tu PC.

## Opcion A: todo con Docker (no necesitas Java)

1. Instala Docker Desktop y abrelo.
2. En la carpeta del backend: `copy .env.example .env` (PowerShell) y completa
   `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL` y `ADMIN_PASSWORD`.
   - Para `JWT_SECRET` usa 48 caracteres al azar, por ejemplo en PowerShell:
     `[Convert]::ToBase64String((1..48 | % { Get-Random -Max 256 }))`
3. `docker compose --profile app up -d --build`
4. La primera vez Postgres crea las tablas solo (`db/migrations`). Revisa con
   `docker compose logs -f api` hasta ver `Started ArriendosApiApplication`.
5. Prueba `http://localhost:8080/actuator/health` (debe decir `UP`) y
   `http://localhost:8080/swagger-ui.html`.
6. En el frontend: `npm start` y entra a `http://localhost:3000` con tu ADMIN.

## Opcion B: BD en Docker y la API desde NetBeans

1. Igual que arriba, pero `docker compose up -d` (solo la BD).
2. Necesitas JDK 17 o superior (Temurin: https://adoptium.net).
3. En la carpeta del backend: `.\mvnw.cmd spring-boot:run`, o Run en NetBeans.
   La API lee el `.env` sola.

## Ver la BD

Cualquier cliente Postgres sirve (DBeaver, pgAdmin, o la extension de VS Code):
host `localhost`, puerto `5432`, base `arriendos`, usuario `arriendos` y tu
`DB_PASSWORD`.

## Tu Oracle anterior

Ya no se usa. Puedes apagarlo con `docker stop bd-pato-oracle-xe`. Los scripts
de Oracle quedan en `db/oracle/` como referencia.

Para partir de cero en Postgres (borra los datos locales): `docker compose down -v`
y vuelve a levantar.
