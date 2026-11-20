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

## Opcion B: BD en Docker y la API desde IntelliJ (o NetBeans)

1. Igual que arriba, pero `docker compose up -d` (solo la BD). Espera a que
   `docker compose ps` muestre `healthy`.
2. IntelliJ: File > Open > carpeta del backend. En File > Project Structure >
   SDK elige un JDK 17 (Add SDK > Download JDK > Eclipse Temurin 17).
   Si avisa de Lombok / annotation processing, acepta Enable.
3. Abre `ArriendosApiApplication.java` y aprieta el boton verde de `main`.
   Debe salir `Started ArriendosApiApplication`. La API lee el `.env` sola
   (el Working directory de la ejecucion debe ser la carpeta del backend).
4. Sin IDE: `.\mvnw.cmd spring-boot:run`.

Si el front dice "No se pudo conectar con el servidor", el back no esta
corriendo: revisa `http://localhost:8080/actuator/health`.

## Fotos en local

Sin `SUPABASE_URL` en el `.env`, las fotos que subas se guardan en la carpeta
`uploads/` del backend y se ven en `http://localhost:8080/uploads/...`. Esa carpeta
no se sube a git. Si quieres probar con Supabase Storage desde tu PC, pon
`SUPABASE_URL` y `SUPABASE_SECRET_KEY` en el `.env`.

## Aplicar una migracion nueva

Postgres solo ejecuta `db/migrations` cuando crea la BD por primera vez. Si ya la
tenias creada y aparece un archivo nuevo (por ejemplo `002_anfitriones_y_galeria.sql`):

- Lo mas simple si no tienes datos que cuidar: `docker compose down -v` y luego
  `docker compose up -d` (borra la BD local y la crea de nuevo con todo).
- Para conservar los datos, en PowerShell desde la carpeta del backend:
  `Get-Content db\migrations\002_anfitriones_y_galeria.sql | docker compose exec -T postgres psql -U arriendos -d arriendos`

## Ver la BD

Cualquier cliente Postgres sirve (DBeaver, pgAdmin, o la extension de VS Code):
host `localhost`, puerto `5432`, base `arriendos`, usuario `arriendos` y tu
`DB_PASSWORD`.

## Tu Oracle anterior

Ya no se usa. Puedes apagarlo con `docker stop bd-pato-oracle-xe`. Los scripts
de Oracle quedan en `db/oracle/` como referencia.

Para partir de cero en Postgres (borra los datos locales): `docker compose down -v`
y vuelve a levantar.
