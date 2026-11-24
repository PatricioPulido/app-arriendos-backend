-- 005: Recuperar contraseña por correo ("¿Olvidaste tu contraseña?").
-- Cada pedido crea un token de un solo uso que vence a los 30 minutos.
-- Igual que con refresh_tokens, solo se guarda el hash SHA-256: si alguien lee
-- la tabla no puede usar los enlaces.
-- Se puede ejecutar mas de una vez (IF NOT EXISTS) y no borra datos.

CREATE SEQUENCE IF NOT EXISTS seq_password_reset;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id_reset       BIGINT      PRIMARY KEY DEFAULT nextval('seq_password_reset'),
  id_usuario     BIGINT      NOT NULL REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
  token_hash     VARCHAR(64) NOT NULL UNIQUE,
  expira         TIMESTAMP   NOT NULL,
  usado          SMALLINT    NOT NULL DEFAULT 0 CHECK (usado IN (0, 1)),
  fecha_uso      TIMESTAMP,
  fecha_creacion TIMESTAMP   NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_password_reset_usuario
  ON password_reset_tokens (id_usuario, fecha_creacion);

-- Igual que las demas tablas: la API REST de Supabase no la ve, solo el backend.
ALTER TABLE password_reset_tokens ENABLE ROW LEVEL SECURITY;
