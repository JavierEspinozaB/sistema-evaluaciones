package com.universidad.tests.domain.port;

import com.universidad.tests.domain.model.SolicitudCalculoSubprueba;

/**
 * Una estrategia por test. El cálculo debe ser idempotente por idSubprueba. No se inventan
 * fórmulas.
 */
public interface EvaluadorSubpruebaPort {
    String codigoTest();

    void calcular(SolicitudCalculoSubprueba solicitud);
}
