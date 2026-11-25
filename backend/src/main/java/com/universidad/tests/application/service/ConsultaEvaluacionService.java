package com.universidad.tests.application.service;

import com.universidad.tests.domain.exception.EvaluacionNoEncontradaException;
import com.universidad.tests.domain.model.Evaluacion;
import com.universidad.tests.domain.repository.EvaluacionRepository;
import com.universidad.tests.dto.response.EvaluacionResponse;
import java.time.Clock;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// E4: estado y progreso de una evaluacion; el tiempo restante lo calcula el servidor
@Service
public class ConsultaEvaluacionService {

    private final EvaluacionRepository evaluacionRepository;
    private final Clock reloj;

    @Autowired
    public ConsultaEvaluacionService(EvaluacionRepository evaluacionRepository) {
        this(evaluacionRepository, Clock.systemUTC());
    }

    // Reloj inyectable para probar el tiempo restante
    public ConsultaEvaluacionService(EvaluacionRepository evaluacionRepository, Clock reloj) {
        this.evaluacionRepository = evaluacionRepository;
        this.reloj = reloj;
    }

    public EvaluacionResponse consultar(Long idEvaluacion) {
        Evaluacion evaluacion =
                evaluacionRepository
                        .buscarPorId(idEvaluacion)
                        .orElseThrow(() -> new EvaluacionNoEncontradaException(idEvaluacion));
        Instant ahora = reloj.instant();

        return new EvaluacionResponse(
                evaluacion.idEvaluacion(),
                evaluacion.idTest(),
                evaluacion.idSujeto(),
                evaluacion.idPeriodo(),
                evaluacion.idVersionFormulario(),
                evaluacion.estado().name(),
                evaluacion.fechaInicio(),
                evaluacion.fechaFin(),
                evaluacion
                        .subpruebaActiva()
                        .map(
                                s ->
                                        new EvaluacionResponse.SubpruebaActiva(
                                                s.codigo(),
                                                s.fechaFinLimite(),
                                                s.segundosRestantes(ahora)))
                        .orElse(null),
                evaluacion.subpruebas().stream()
                        .map(
                                s ->
                                        new EvaluacionResponse.SubpruebaEstado(
                                                s.codigo(), s.estado().name(), s.fechaCierre()))
                        .toList());
    }
}
