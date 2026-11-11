-- GEN-03-BE: entrega durable al evaluador, sin fórmulas de puntuación.
CREATE TABLE solicitud_calculo_subprueba (
    id_subprueba BIGINT PRIMARY KEY REFERENCES evaluacion_subprueba(id) ON DELETE CASCADE,
    evaluacion_id VARCHAR(50) NOT NULL REFERENCES evaluacion(id) ON DELETE CASCADE,
    codigo_test VARCHAR(80) NOT NULL,
    codigo_subprueba VARCHAR(50) NOT NULL,
    fecha_cierre TIMESTAMPTZ NOT NULL,
    procesada_en TIMESTAMPTZ
);
CREATE INDEX idx_solicitud_calculo_pendiente ON solicitud_calculo_subprueba(fecha_cierre,id_subprueba) WHERE procesada_en IS NULL;
ALTER TABLE respuesta_evaluacion ADD CONSTRAINT fk_respuesta_subprueba FOREIGN KEY(evaluacion_id,subprueba_codigo) REFERENCES evaluacion_subprueba(evaluacion_id,codigo) ON DELETE CASCADE;
CREATE UNIQUE INDEX uq_opcion_pregunta_aplicacion ON opcion_respuesta(id_opcion,id_pregunta);
ALTER TABLE respuesta_evaluacion ADD CONSTRAINT fk_respuesta_opcion_pregunta FOREIGN KEY(opcion_id,pregunta_id) REFERENCES opcion_respuesta(id_opcion,id_pregunta);
CREATE UNIQUE INDEX uq_evaluacion_subprueba_activa ON evaluacion_subprueba(evaluacion_id) WHERE estado='ACTIVA';
ALTER TABLE evaluacion_subprueba ADD CONSTRAINT ck_estado_aplicacion CHECK(estado IN ('PENDIENTE','ACTIVA','CERRADA'));
ALTER TABLE evaluacion_subprueba ADD CONSTRAINT ck_tiempo_aplicacion CHECK((estado='PENDIENTE' AND fecha_inicio IS NULL AND fecha_fin_limite IS NULL AND fecha_cierre IS NULL) OR (estado='ACTIVA' AND fecha_inicio IS NOT NULL AND fecha_fin_limite>fecha_inicio AND fecha_cierre IS NULL) OR (estado='CERRADA' AND fecha_inicio IS NOT NULL AND fecha_fin_limite>fecha_inicio AND fecha_cierre IS NOT NULL));
