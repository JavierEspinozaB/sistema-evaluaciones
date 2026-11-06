package com.universidad.tests.domain.repository;

import com.universidad.tests.domain.model.Pregunta;
import java.util.List;
import java.util.Optional;

public interface PreguntaRepository {
    List<Pregunta> listarPorSubprueba(String subpruebaCodigo);

    Optional<Pregunta> buscarPorId(String id);
}
