package com.universidad.tests.domain.repository;

import com.universidad.tests.domain.model.PreguntaAplicacion;
import java.util.List;
import java.util.Optional;

public interface PreguntaRepositoryAplicacion {
    List<PreguntaAplicacion> listarPorSubprueba(String subpruebaCodigo);

    Optional<PreguntaAplicacion> buscarPorId(String id);
}
