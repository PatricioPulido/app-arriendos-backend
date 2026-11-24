-- 004: Ficha del alojamiento estilo Airbnb/Booking.
-- Agrega a cada propiedad: comodidades, "lo mejor de este lugar", horarios de
-- check-in/out, reglas de la casa y un texto sobre la zona.
-- Se puede ejecutar mas de una vez (IF NOT EXISTS) y no borra datos.
--
-- comodidades y destacados guardan CODIGOS ('wifi', 'piscina', ...). El texto y el
-- icono de cada codigo estan en el front (src/ficha/catalogo.js), asi se pueden
-- cambiar sin tocar la BD. Un codigo que el front no conoce simplemente no se muestra.

ALTER TABLE departamentos
  ADD COLUMN IF NOT EXISTS comodidades        text[]       NOT NULL DEFAULT '{}',
  ADD COLUMN IF NOT EXISTS destacados         text[]       NOT NULL DEFAULT '{}',
  ADD COLUMN IF NOT EXISTS hora_check_in      varchar(5),
  ADD COLUMN IF NOT EXISTS hora_check_out     varchar(5),
  -- NULL = el anfitrion no lo indico (la ficha no muestra nada)
  ADD COLUMN IF NOT EXISTS permite_mascotas   boolean,
  ADD COLUMN IF NOT EXISTS permite_fumar      boolean,
  ADD COLUMN IF NOT EXISTS permite_fiestas    boolean,
  ADD COLUMN IF NOT EXISTS reglas_adicionales varchar(1000),
  ADD COLUMN IF NOT EXISTS sobre_la_zona      varchar(1000);

-- Reglas que la BD hace cumplir aunque alguien se salte la API.
-- (El DO ... revisa si ya existen, para poder ejecutar el script dos veces.)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_departamentos_ficha') THEN
    ALTER TABLE departamentos ADD CONSTRAINT ck_departamentos_ficha CHECK (
      (hora_check_in  IS NULL OR hora_check_in  ~ '^([01][0-9]|2[0-3]):[0-5][0-9]$') AND
      (hora_check_out IS NULL OR hora_check_out ~ '^([01][0-9]|2[0-3]):[0-5][0-9]$') AND
      cardinality(destacados) <= 3 AND
      cardinality(comodidades) <= 80);
  END IF;
END $$;

-- Para el futuro filtro "con piscina", "con estacionamiento", etc.
CREATE INDEX IF NOT EXISTS idx_departamentos_comodidades ON departamentos USING gin (comodidades);
