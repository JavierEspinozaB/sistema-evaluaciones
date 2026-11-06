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

CREATE TABLE pregunta_catalogo (
    id VARCHAR(50) PRIMARY KEY,
    subprueba_codigo VARCHAR(50) NOT NULL,
    numero INT NOT NULL,
    enunciado TEXT NOT NULL,
    recurso_multimedia TEXT,
    tipo_respuesta VARCHAR(30) NOT NULL DEFAULT 'SELECCION',
    clave_correcta VARCHAR(255),
    orden INT NOT NULL
);

CREATE TABLE opcion_pregunta (
    id VARCHAR(50) PRIMARY KEY,
    pregunta_id VARCHAR(50) NOT NULL REFERENCES pregunta_catalogo(id) ON DELETE CASCADE,
    etiqueta VARCHAR(10) NOT NULL,
    texto TEXT NOT NULL,
    orden INT NOT NULL
);

CREATE TABLE respuesta_evaluacion (
    id BIGSERIAL PRIMARY KEY,
    evaluacion_id VARCHAR(50) NOT NULL REFERENCES evaluacion(id) ON DELETE CASCADE,
    subprueba_codigo VARCHAR(50) NOT NULL,
    pregunta_id VARCHAR(50) NOT NULL REFERENCES pregunta_catalogo(id),
    opcion_id VARCHAR(50),
    valor_texto TEXT,
    orden_entrada INT NOT NULL DEFAULT 1,
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_respuesta_pregunta UNIQUE (evaluacion_id, subprueba_codigo, pregunta_id, orden_entrada)
);

CREATE INDEX idx_eval_subprueba_eval_id ON evaluacion_subprueba(evaluacion_id);
CREATE INDEX idx_pregunta_subprueba ON pregunta_catalogo(subprueba_codigo);
CREATE INDEX idx_opcion_pregunta ON opcion_pregunta(pregunta_id);
CREATE INDEX idx_respuesta_eval ON respuesta_evaluacion(evaluacion_id, subprueba_codigo);

