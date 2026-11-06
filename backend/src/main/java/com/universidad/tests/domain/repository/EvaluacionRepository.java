package com.universidad.tests.domain.repository;

import com.universidad.tests.domain.model.Evaluacion;
import com.universidad.tests.domain.model.EvaluacionSubprueba;
import java.util.Optional;

public interface EvaluacionRepository {
    Optional<Evaluacion> buscarPorId(String id);

    Evaluacion guardar(Evaluacion evaluacion);

    Optional<EvaluacionSubprueba> buscarSubprueba(String evaluacionId, String subpruebaCodigo);

    void guardarSubprueba(EvaluacionSubprueba subprueba);
}

