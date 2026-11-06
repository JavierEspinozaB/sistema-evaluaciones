package com.universidad.tests.domain.repository;

import com.universidad.tests.domain.model.RespuestaEvaluacion;
import java.util.List;
import java.util.Optional;

public interface RespuestaRepository {
    List<RespuestaEvaluacion> listarPorEvaluacionYSubprueba(
            String evaluacionId, String subpruebaCodigo);

    Optional<RespuestaEvaluacion> buscarRespuesta(
            String evaluacionId, String subpruebaCodigo, String preguntaId, int ordenEntrada);

    RespuestaEvaluacion guardar(RespuestaEvaluacion respuesta);

    long contarPorEvaluacionYSubprueba(String evaluacionId, String subpruebaCodigo);
}
