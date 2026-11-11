-- Migración comun para motor de subpruebas (GEN-03)

CREATE TABLE evaluacion (
    id VARCHAR(50) PRIMARY KEY,
    sujeto_id VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE evaluacion_subprueba (
    id BIGSERIAL PRIMARY KEY,
    evaluacion_id VARCHAR(50) NOT NULL REFERENCES evaluacion(id) ON DELETE CASCADE,
    configuracion_id BIGINT NOT NULL REFERENCES configuracion_subprueba(id_configuracion_subprueba),
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    orden INT NOT NULL,
    duracion_segundos INT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    instrucciones TEXT,
    permitir_retroceso BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_inicio TIMESTAMP WITH TIME ZONE,
    fecha_fin_limite TIMESTAMP WITH TIME ZONE,
    fecha_cierre TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_eval_subprueba UNIQUE (evaluacion_id, codigo)
);

CREATE TABLE respuesta_evaluacion (
    id BIGSERIAL PRIMARY KEY,
    evaluacion_id VARCHAR(50) NOT NULL REFERENCES evaluacion(id) ON DELETE CASCADE,
    subprueba_codigo VARCHAR(50) NOT NULL,
    pregunta_id BIGINT NOT NULL REFERENCES pregunta(id_pregunta),
    opcion_id BIGINT REFERENCES opcion_respuesta(id_opcion),
    valor_texto TEXT,
    orden_entrada INT NOT NULL DEFAULT 1,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_respuesta_pregunta UNIQUE (evaluacion_id, subprueba_codigo, pregunta_id, orden_entrada)
);

CREATE INDEX idx_eval_subprueba_eval_id ON evaluacion_subprueba(evaluacion_id);
CREATE INDEX idx_respuesta_eval ON respuesta_evaluacion(evaluacion_id, subprueba_codigo);

