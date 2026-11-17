-- Agrega las columnas que el front ya usa (foto principal y precio por noche en CLP).
-- Ejecutar una vez como el usuario PATO antes de desplegar este cambio.
ALTER TABLE DEPARTAMENTOS ADD (
  IMAGEN_URL VARCHAR2(500),
  PRECIO     NUMBER(12) CHECK (PRECIO >= 0)
);
