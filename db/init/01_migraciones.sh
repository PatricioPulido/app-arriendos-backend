#!/bin/bash
# Lo ejecuta el contenedor de Oracle SOLO la primera vez que crea la BD (volumen vacio).
# Corre las migraciones como el usuario de la app, en orden (000, 001, 002...).
set -e
for f in /db-migrations/*.sql; do
  echo "Aplicando $f"
  { echo "WHENEVER SQLERROR EXIT SQL.SQLCODE"; cat "$f"; echo; echo "EXIT"; } \
    | sqlplus -s "${APP_USER}/${APP_USER_PASSWORD}@//localhost/XEPDB1"
done
