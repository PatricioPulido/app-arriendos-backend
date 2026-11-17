-- Esquema completo de la app en PostgreSQL (Supabase o Postgres local).
-- Equivale a los scripts de Oracle 000 + 001 + 002 (ver db/oracle/).
-- Se ejecuta una sola vez sobre una BD vacia.

CREATE SEQUENCE seq_ciudad;
CREATE TABLE ciudad (
  id_ciudad BIGINT       PRIMARY KEY,
  nombre    VARCHAR(100) NOT NULL
);

CREATE SEQUENCE seq_zona;
CREATE TABLE zona (
  id_zona   BIGINT       PRIMARY KEY,
  nombre    VARCHAR(100) NOT NULL,
  id_ciudad BIGINT       NOT NULL REFERENCES ciudad (id_ciudad)
);
CREATE INDEX ix_zona_ciudad ON zona (id_ciudad);

CREATE SEQUENCE seq_departamentos;
CREATE TABLE departamentos (
  id_departamento  BIGINT        PRIMARY KEY,
  nombre           VARCHAR(150)  NOT NULL,
  numero           INTEGER,
  direccion        VARCHAR(255),
  metros_cuadrados NUMERIC(8,2),
  tipologia        VARCHAR(50),
  imagen_url       VARCHAR(500),
  precio           BIGINT        CHECK (precio >= 0)   -- CLP por noche
);

CREATE SEQUENCE seq_arrendatarios;
CREATE TABLE arrendatarios (
  id_arrendatario  BIGINT       PRIMARY KEY,
  nombre           VARCHAR(100) NOT NULL,
  apellido         VARCHAR(100) NOT NULL,
  email            VARCHAR(255),
  telefono         VARCHAR(30),
  fecha_nacimiento DATE,
  id_departamento  BIGINT       REFERENCES departamentos (id_departamento)
);

CREATE SEQUENCE seq_reservas;
CREATE TABLE reservas (
  id_reserva      BIGINT      PRIMARY KEY,
  id_departamento BIGINT      NOT NULL REFERENCES departamentos (id_departamento),
  id_arrendatario BIGINT      NOT NULL REFERENCES arrendatarios (id_arrendatario),
  fecha_inicio    DATE        NOT NULL,
  fecha_fin       DATE        NOT NULL,
  estado          VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                  CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'CANCELADA', 'FINALIZADA')),
  CHECK (fecha_fin > fecha_inicio)
);
CREATE INDEX ix_reservas_depto ON reservas (id_departamento, fecha_inicio);
CREATE INDEX ix_reservas_arrendatario ON reservas (id_arrendatario);

-- Usuarios del login
CREATE SEQUENCE seq_usuarios;
CREATE TABLE usuarios (
  id_usuario      BIGINT       PRIMARY KEY,
  email           VARCHAR(255) NOT NULL,
  password_hash   VARCHAR(100) NOT NULL,
  rol             VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMIN', 'ARRENDATARIO')),
  id_arrendatario BIGINT       REFERENCES arrendatarios (id_arrendatario),
  activo          SMALLINT     NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
  fecha_creacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- Email unico sin importar mayusculas
CREATE UNIQUE INDEX ux_usuarios_email ON usuarios (LOWER(email));

-- Solo se guarda el hash SHA-256 del token, nunca el token en claro
CREATE SEQUENCE seq_refresh_tokens;
CREATE TABLE refresh_tokens (
  id_refresh_token BIGINT      PRIMARY KEY,
  id_usuario       BIGINT      NOT NULL REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
  token_hash       VARCHAR(64) NOT NULL UNIQUE,
  expira           TIMESTAMP   NOT NULL,
  revocado         SMALLINT    NOT NULL DEFAULT 0 CHECK (revocado IN (0, 1)),
  fecha_creacion   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_refresh_tokens_usuario ON refresh_tokens (id_usuario);

-- Supabase expone las tablas de "public" por su API REST. Esta app solo entra por
-- el backend, asi que se activa RLS sin politicas: la API REST de Supabase no ve
-- nada y el backend (que se conecta como dueno de las tablas) sigue funcionando.
ALTER TABLE ciudad         ENABLE ROW LEVEL SECURITY;
ALTER TABLE zona           ENABLE ROW LEVEL SECURITY;
ALTER TABLE departamentos  ENABLE ROW LEVEL SECURITY;
ALTER TABLE arrendatarios  ENABLE ROW LEVEL SECURITY;
ALTER TABLE reservas       ENABLE ROW LEVEL SECURITY;
ALTER TABLE usuarios       ENABLE ROW LEVEL SECURITY;
ALTER TABLE refresh_tokens ENABLE ROW LEVEL SECURITY;
