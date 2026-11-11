ALTER TABLE evaluacion_subprueba ADD COLUMN letra_inicial VARCHAR(1);
ALTER TABLE evaluacion_subprueba ADD COLUMN maximo_entradas INTEGER NOT NULL DEFAULT 75 CHECK (maximo_entradas BETWEEN 1 AND 75);
ALTER TABLE respuesta_evaluacion ALTER COLUMN pregunta_id DROP NOT NULL;
CREATE UNIQUE INDEX uq_respuesta_fv_orden ON respuesta_evaluacion (evaluacion_id, subprueba_codigo, orden_entrada) WHERE pregunta_id IS NULL;
ALTER TABLE respuesta_evaluacion ADD CONSTRAINT ck_respuesta_fv CHECK (pregunta_id IS NOT NULL OR (upper(subprueba_codigo) = 'FV' AND orden_entrada BETWEEN 1 AND 75 AND valor_texto IS NOT NULL AND length(trim(valor_texto)) > 0));
ALTER TABLE evaluacion ADD COLUMN test_id VARCHAR(255), ADD COLUMN periodo_id VARCHAR(255), ADD COLUMN version_formulario_id VARCHAR(255), ADD COLUMN fecha_inicio TIMESTAMPTZ, ADD COLUMN fecha_fin TIMESTAMPTZ;
UPDATE evaluacion SET fecha_inicio = creado_en;
