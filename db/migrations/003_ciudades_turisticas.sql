-- Ciudades y zonas turisticas iniciales (las que usa "Publica tu propiedad").
-- En Supabase ya se cargo el 2026-11-23; no volver a ejecutarlo alla.
-- Para sumar mas: INSERT en ciudad y zona con nextval('seq_ciudad') / nextval('seq_zona').
WITH datos(ciudad, zonas) AS (VALUES
  ('Viña del Mar',      ARRAY['Reñaca','Centro','Jardín del Mar','Recreo']),
  ('Concón',            ARRAY['Costa Brava','Playa Amarilla','Bosques de Montemar']),
  ('Valparaíso',        ARRAY['Cerro Alegre','Cerro Concepción','Playa Ancha']),
  ('Puchuncaví',        ARRAY['Maitencillo','Horcón']),
  ('Zapallar',          ARRAY['Zapallar','Cachagua']),
  ('Papudo',            ARRAY['Papudo Centro','Pullally']),
  ('Algarrobo',         ARRAY['El Canelo','Mirasol','San Alfonso del Mar','Algarrobo Centro']),
  ('El Quisco',         ARRAY['El Quisco Centro','Isla Negra','Punta de Tralca']),
  ('El Tabo',           ARRAY['El Tabo Centro','Las Cruces']),
  ('Santiago',          ARRAY['Providencia','Las Condes','Vitacura','Ñuñoa','Santiago Centro','Lo Barnechea']),
  ('Cajón del Maipo',   ARRAY['San José de Maipo','San Alfonso','Baños Morales']),
  ('Pirque',            ARRAY['Pirque Centro','El Principal']),
  ('Temuco',            ARRAY['Temuco Centro','Labranza']),
  ('Villarrica',        ARRAY['Villarrica Centro','Lican Ray']),
  ('Pucón',             ARRAY['Pucón Centro','Caburgua']),
  ('Valdivia',          ARRAY['Valdivia Centro','Niebla','Isla Teja']),
  ('Máfil',             ARRAY['Máfil Centro']),
  ('Puerto Varas',      ARRAY['Puerto Varas Centro','Ensenada','Frutillar'])
), c AS (
  INSERT INTO ciudad (id_ciudad, nombre)
  SELECT nextval('seq_ciudad'), ciudad FROM datos
  RETURNING id_ciudad, nombre
)
INSERT INTO zona (id_zona, nombre, id_ciudad)
SELECT nextval('seq_zona'), z, c.id_ciudad
FROM c JOIN datos d ON d.ciudad = c.nombre, unnest(d.zonas) AS z;
