package com.universidad.tests.application.service;

import com.universidad.tests.domain.exception.RecursoNoEncontradoException;
import com.universidad.tests.domain.exception.RespuestaInvalidaException;
import com.universidad.tests.domain.exception.SolicitudInvalidaException;
import com.universidad.tests.domain.exception.SubpruebaCerradaException;
import com.universidad.tests.domain.exception.SubpruebaNoActivaException;
import com.universidad.tests.domain.exception.TiempoAgotadoException;
import com.universidad.tests.domain.model.EstadoSubprueba;
import com.universidad.tests.domain.model.Evaluacion;
import com.universidad.tests.domain.model.EvaluacionSubprueba;
import com.universidad.tests.domain.model.Pregunta;
import com.universidad.tests.domain.model.RespuestaEvaluacion;
import com.universidad.tests.domain.model.TipoRespuesta;
import com.universidad.tests.domain.repository.EvaluacionRepository;
import com.universidad.tests.domain.repository.PreguntaRepository;
import com.universidad.tests.domain.repository.RespuestaRepository;
import com.universidad.tests.dto.AvanceSubpruebaResponseDto;
import com.universidad.tests.dto.EvaluacionResponseDto;
import com.universidad.tests.dto.PreguntasSubpruebaResponseDto;
import com.universidad.tests.dto.RegistrarRespuestaRequestDto;
import com.universidad.tests.dto.SubpruebaResponseDto;
import com.universidad.tests.mapper.MotorSubpruebasMapper;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MotorSubpruebasService {

    private final EvaluacionRepository evaluacionRepository;
    private final PreguntaRepository preguntaRepository;
    private final RespuestaRepository respuestaRepository;
    private final MotorSubpruebasMapper mapper;
    private final Clock clock;

    public MotorSubpruebasService(
            EvaluacionRepository evaluacionRepository,
            PreguntaRepository preguntaRepository,
            RespuestaRepository respuestaRepository,
            MotorSubpruebasMapper mapper,
            Clock clock) {
        this.evaluacionRepository = evaluacionRepository;
        this.preguntaRepository = preguntaRepository;
        this.respuestaRepository = respuestaRepository;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public EvaluacionResponseDto consultarEvaluacion(String evaluacionId) {
        Instant ahora = clock.instant();
        Evaluacion evaluacion = obtenerEvaluacionOError(evaluacionId);

        // Auto-cerrar cualquier subprueba expirada
        for (EvaluacionSubprueba sub : evaluacion.getSubpruebas()) {
            if (sub.getEstado() == EstadoSubprueba.ACTIVA && sub.haExpirado(ahora)) {
                sub.cerrar(ahora);
                evaluacionRepository.guardarSubprueba(sub);
            }
        }

        return mapper.toEvaluacionDto(evaluacion, ahora);
    }

    public SubpruebaResponseDto iniciarSubprueba(String evaluacionId, String subpruebaCodigo) {
        Instant ahora = clock.instant();
        Evaluacion evaluacion = obtenerEvaluacionOError(evaluacionId);
        EvaluacionSubprueba subprueba = evaluacion.buscarSubprueba(subpruebaCodigo);

        if (subprueba.getEstado() == EstadoSubprueba.CERRADA) {
            throw new SubpruebaCerradaException(
                    "La subprueba " + subpruebaCodigo + " ya ha sido cerrada.");
        }

        if (subprueba.getEstado() == EstadoSubprueba.ACTIVA) {
            if (subprueba.haExpirado(ahora)) {
                subprueba.cerrar(ahora);
                evaluacionRepository.guardarSubprueba(subprueba);
                throw new SubpruebaCerradaException(
                        "La subprueba " + subpruebaCodigo + " ya ha finalizado su tiempo.");
            }
            // Idempotente: CU-BFA-P03
            return mapper.toSubpruebaDto(subprueba, ahora);
        }

        // Validar orden estricto
        evaluacion.validarOrdenSubprueba(subpruebaCodigo);

        // Validar que ninguna otra subprueba esté activa
        for (EvaluacionSubprueba sub : evaluacion.getSubpruebas()) {
            if (sub.getEstado() == EstadoSubprueba.ACTIVA) {
                if (sub.haExpirado(ahora)) {
                    sub.cerrar(ahora);
                    evaluacionRepository.guardarSubprueba(sub);
                }
            }
        }

        subprueba.iniciar(ahora);
        evaluacionRepository.guardarSubprueba(subprueba);

        return mapper.toSubpruebaDto(subprueba, ahora);
    }

    public PreguntasSubpruebaResponseDto consultarPreguntas(
            String evaluacionId, String subpruebaCodigo) {
        Instant ahora = clock.instant();
        Evaluacion evaluacion = obtenerEvaluacionOError(evaluacionId);
        EvaluacionSubprueba subprueba = evaluacion.buscarSubprueba(subpruebaCodigo);

        if (subprueba.getEstado() == EstadoSubprueba.ACTIVA && subprueba.haExpirado(ahora)) {
            subprueba.cerrar(ahora);
            evaluacionRepository.guardarSubprueba(subprueba);
            throw new SubpruebaNoActivaException(
                    "El tiempo de la subprueba " + subpruebaCodigo + " ha finalizado.");
        }

        if (subprueba.getEstado() != EstadoSubprueba.ACTIVA) {
            throw new SubpruebaNoActivaException(
                    "La subprueba " + subpruebaCodigo + " no se encuentra activa.");
        }

        List<Pregunta> preguntas = preguntaRepository.listarPorSubprueba(subpruebaCodigo);
        List<RespuestaEvaluacion> respuestas =
                respuestaRepository.listarPorEvaluacionYSubprueba(evaluacionId, subpruebaCodigo);

        return mapper.toPreguntasSubpruebaDto(subprueba, preguntas, respuestas, ahora);
    }

    public AvanceSubpruebaResponseDto registrarRespuesta(
            String evaluacionId, String subpruebaCodigo, RegistrarRespuestaRequestDto request) {
        Instant ahora = clock.instant();

        if (request == null || request.preguntaId() == null || request.preguntaId().isBlank()) {
            throw new SolicitudInvalidaException(
                    "El identificador de pregunta es obligatorio.",
                    List.of("preguntaId no puede ser nulo ni vacío"));
        }

        Evaluacion evaluacion = obtenerEvaluacionOError(evaluacionId);
        EvaluacionSubprueba subprueba = evaluacion.buscarSubprueba(subpruebaCodigo);

        if (subprueba.getEstado() == EstadoSubprueba.CERRADA) {
            throw new SubpruebaNoActivaException(
                    "La subprueba " + subpruebaCodigo + " se encuentra cerrada.");
        }

        if (subprueba.getEstado() != EstadoSubprueba.ACTIVA) {
            throw new SubpruebaNoActivaException(
                    "La subprueba " + subpruebaCodigo + " no está activa.");
        }

        if (subprueba.haExpirado(ahora)) {
            subprueba.cerrar(ahora);
            evaluacionRepository.guardarSubprueba(subprueba);
            throw new TiempoAgotadoException(
                    "El tiempo límite para la subprueba " + subpruebaCodigo + " ha finalizado.");
        }

        // Validar pregunta
        Pregunta pregunta =
                preguntaRepository
                        .buscarPorId(request.preguntaId())
                        .orElseThrow(
                                () ->
                                        new RespuestaInvalidaException(
                                                "Pregunta inexistente: " + request.preguntaId()));

        if (!pregunta.getSubpruebaCodigo().equalsIgnoreCase(subpruebaCodigo)) {
            throw new RespuestaInvalidaException(
                    "La pregunta "
                            + request.preguntaId()
                            + " no pertenece a la subprueba "
                            + subpruebaCodigo);
        }

        if (pregunta.getTipoRespuesta() == TipoRespuesta.SELECCION) {
            if (request.opcionId() != null && !pregunta.tieneOpcion(request.opcionId())) {
                throw new RespuestaInvalidaException(
                        "La opción seleccionada no pertenece a la pregunta.");
            }
        }

        int ordenEntrada = request.ordenEntrada() == null ? 1 : request.ordenEntrada();

        // Idempotencia: buscar si ya existe respuesta previa
        Optional<RespuestaEvaluacion> existente =
                respuestaRepository.buscarRespuesta(
                        evaluacionId, subpruebaCodigo, request.preguntaId(), ordenEntrada);

        Long respuestaId = existente.map(RespuestaEvaluacion::getId).orElse(null);

        RespuestaEvaluacion entidadAGuardar =
                new RespuestaEvaluacion(
                        respuestaId,
                        evaluacionId,
                        subpruebaCodigo,
                        request.preguntaId(),
                        request.opcionId(),
                        request.valorTexto(),
                        ordenEntrada,
                        ahora);

        respuestaRepository.guardar(entidadAGuardar);

        long totalRespondidas =
                respuestaRepository.contarPorEvaluacionYSubprueba(evaluacionId, subpruebaCodigo);
        int totalPreguntas = preguntaRepository.listarPorSubprueba(subpruebaCodigo).size();

        return new AvanceSubpruebaResponseDto(
                evaluacionId,
                subpruebaCodigo,
                request.preguntaId(),
                totalRespondidas,
                totalPreguntas,
                true);
    }

    private Evaluacion obtenerEvaluacionOError(String evaluacionId) {
        return evaluacionRepository
                .buscarPorId(evaluacionId)
                .orElseThrow(
                        () ->
                                new RecursoNoEncontradoException(
                                        "EVALUACION_NO_ENCONTRADA",
                                        "Evaluación no encontrada: " + evaluacionId));
    }
}

