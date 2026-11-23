# Levantar todo en tu PC

## Opcion A: todo con Docker (no necesitas Java)

1. Instala Docker Desktop y abrelo.
2. En la carpeta del backend: `copy .env.example .env` (PowerShell) y completa
   `ORACLE_PASSWORD`, `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`.
   - Para `JWT_SECRET` usa 48 caracteres al azar, por ejemplo en PowerShell:
     `[Convert]::ToBase64String((1..48 | % { Get-Random -Max 256 }))`
3. `docker compose --profile app up -d --build`
4. Espera 1-3 minutos (la primera vez Oracle crea la BD y aplica `db/migrations`).
   Revisa con `docker compose logs -f api` hasta ver `Started ArriendosApiApplication`.
5. Prueba `http://localhost:8080/actuator/health` (debe decir `UP`) y
   `http://localhost:8080/swagger-ui.html`.
6. En el frontend: `npm start` y entra a `http://localhost:3000` con tu ADMIN.

## Opcion B: BD en Docker y la API desde NetBeans

1. Igual que arriba, pero `docker compose up -d` (solo la BD).
2. Necesitas JDK 17 o superior (Temurin: https://adoptium.net).
3. En la carpeta del backend: `.\mvnw.cmd spring-boot:run`, o Run en NetBeans.
   La API lee el `.env` sola.

## Si ya tenias tu Oracle con datos

Las migraciones automaticas solo corren cuando el volumen es NUEVO, asi que no
tocan tu BD actual. Si tu usuario PATO vive en `XE` (no en `XEPDB1`), usa
`DB_URL=jdbc:oracle:thin:@//localhost:1521/XE` y aplica a mano solo `001` y `002`
si aun no los corriste.

Para partir de cero (borra los datos): `docker compose down -v` y vuelve a levantar.
