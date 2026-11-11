package com.universidad.tests.domain.port;

/**
 * GEN-06 debe comprobar identidad, permiso y alcance sobre la evaluación antes de consultar datos.
 */
public interface AutorizacionEvaluacionPort {
    void exigirAcceso(String evaluacionId);
}
