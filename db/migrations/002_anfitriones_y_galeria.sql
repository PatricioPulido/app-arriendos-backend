-- Cuentas de anfitrion, datos de la propiedad y galeria de imagenes.
-- Se aplica despues de 001. En una BD local nueva, docker compose la aplica sola;
-- si tu BD ya existe, ver docs/LOCAL.md ("Aplicar una migracion nueva").

-- 1) Nuevo rol: ANFITRION (publica alojamientos y tambien puede reservar)
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_rol_check;
ALTER TABLE usuarios ADD CONSTRAINT usuarios_rol_check
  CHECK (rol IN ('ADMIN', 'ARRENDATARIO', 'ANFITRION'));

-- 2) Refresh tokens: cuando se revoco, para tolerar dos pestanas renovando a la vez
ALTER TABLE refresh_tokens ADD COLUMN fecha_revocacion TIMESTAMP;

-- 3) Datos de la propiedad: dueno, ubicacion (para filtros) y detalles
ALTER TABLE departamentos
  ADD COLUMN id_anfitrion   BIGINT REFERENCES usuarios (id_usuario),
  ADD COLUMN id_zona        BIGINT REFERENCES zona (id_zona),
  ADD COLUMN descripcion    VARCHAR(2000),
  ADD COLUMN capacidad      SMALLINT CHECK (capacidad > 0),
  ADD COLUMN dormitorios    SMALLINT CHECK (dormitorios >= 0),
  ADD COLUMN banos          SMALLINT CHECK (banos >= 0),
  ADD COLUMN fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
CREATE INDEX ix_departamentos_anfitrion ON departamentos (id_anfitrion);
CREATE INDEX ix_departamentos_zona ON departamentos (id_zona);
CREATE INDEX ix_departamentos_precio ON departamentos (precio);

-- 4) Galeria: varias fotos por propiedad. La primera (orden mas bajo) es la portada.
--    "ruta" es la ruta del archivo en el bucket (o en la carpeta local), "url" la publica.
CREATE SEQUENCE seq_departamento_imagenes;
CREATE TABLE departamento_imagenes (
  id_imagen       BIGINT        PRIMARY KEY,
  id_departamento BIGINT        NOT NULL REFERENCES departamentos (id_departamento) ON DELETE CASCADE,
  ruta            VARCHAR(500)  NOT NULL UNIQUE,
  url             VARCHAR(1000) NOT NULL,
  orden           INTEGER       NOT NULL DEFAULT 0,
  fecha_creacion  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_imagenes_departamento ON departamento_imagenes (id_departamento, orden);

ALTER TABLE departamento_imagenes ENABLE ROW LEVEL SECURITY;
