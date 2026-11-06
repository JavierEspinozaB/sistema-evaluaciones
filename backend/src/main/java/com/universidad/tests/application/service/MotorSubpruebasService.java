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
import com.universidad.tests.domain.model.Opcion;
import com.universidad.tests.domain.model.Pregunta;
import com.universidad.tests.domain.model.RespuestaEvaluacion;
import com.universidad.tests.domain.model.TipoRespuesta;
import com.universidad.tests.domain.repository.EvaluacionRepository;
import com.universidad.tests.domain.repository.PreguntaRepository;
import com.universidad.tests.domain.repository.RespuestaRepository;
import com.universidad.tests.dto.AvanceSubpruebaResponseDto;
import com.universidad.tests.dto.EvaluacionResponseDto;
import com.universidad.tests.dto.ItemRespuestaDto;
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
@Transactional(
        noRollbackFor = {
            TiempoAgotadoException.class,
            SubpruebaCerradaException.class,
            SubpruebaNoActivaException.class
        })
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

    public EvaluacionResponseDto consultarEvaluacion(String evaluacionId) {
        Instant ahora = clock.instant();
        Evaluacion evaluacion = obtenerEvaluacionOError(evaluacionId);

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

        // 1. Auto-cerrar cualquier subprueba ACTIVA que haya expirado
        for (EvaluacionSubprueba sub : evaluacion.getSubpruebas()) {
            if (sub.getEstado() == EstadoSubprueba.ACTIVA && sub.haExpirado(ahora)) {
                sub.cerrar(ahora);
                evaluacionRepository.guardarSubprueba(sub);
            }
        }

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
            return mapper.toSubpruebaDto(subprueba, ahora);
        }

        // 2. Validar orden estricto (ahora que las expiradas ya fueron cerradas)
        evaluacion.validarOrdenSubprueba(subpruebaCodigo);

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

        List<ItemRespuestaDto> items =
                request != null ? request.obtenerListaRespuestas() : List.of();
        if (items.isEmpty()) {
            throw new SolicitudInvalidaException(
                    "El identificador de pregunta es obligatorio.",
                    "preguntaId",
                    "preguntaId es obligatorio");
        }

        for (ItemRespuestaDto item : items) {
            if (item.preguntaId() == null || item.preguntaId().isBlank()) {
                throw new SolicitudInvalidaException(
                        "El identificador de pregunta es obligatorio.",
                        "preguntaId",
                        "preguntaId es obligatorio");
            }

            Pregunta pregunta =
                    preguntaRepository
                            .buscarPorId(item.preguntaId())
                            .orElseThrow(
                                    () ->
                                            new RespuestaInvalidaException(
                                                    "preguntaId",
                                                    "Pregunta inexistente: " + item.preguntaId()));

            if (!pregunta.getSubpruebaCodigo().equalsIgnoreCase(subpruebaCodigo)) {
                throw new RespuestaInvalidaException(
                        "preguntaId",
                        "La pregunta "
                                + item.preguntaId()
                                + " no pertenece a la subprueba "
                                + subpruebaCodigo);
            }

            String opcionIdAGuardar = null;
            String valorTextoAGuardar = item.valorTexto();
            String opcionEntrada = item.opcion() != null ? item.opcion() : item.opcionId();

            if (pregunta.getTipoRespuesta() == TipoRespuesta.SELECCION) {
                if (opcionEntrada == null || opcionEntrada.isBlank()) {
                    throw new RespuestaInvalidaException("opcion", "La opción es obligatoria.");
                }
                Optional<Opcion> match =
                        pregunta.getOpciones().stream()
                                .filter(
                                        o ->
                                                o.getEtiqueta().equalsIgnoreCase(opcionEntrada)
                                                        || o.getId()
                                                                .equalsIgnoreCase(opcionEntrada))
                                .findFirst();
                if (match.isEmpty()) {
                    throw new RespuestaInvalidaException(
                            "opcion", "La opción seleccionada no pertenece a la pregunta.");
                }
                opcionIdAGuardar = match.get().getId();
            } else {
                if (valorTextoAGuardar == null && opcionEntrada != null) {
                    valorTextoAGuardar = opcionEntrada;
                }
            }

            int ordenEntrada = item.ordenEntrada() == null ? 1 : item.ordenEntrada();

            Optional<RespuestaEvaluacion> existente =
                    respuestaRepository.buscarRespuesta(
                            evaluacionId, subpruebaCodigo, item.preguntaId(), ordenEntrada);

            Long respuestaId = existente.map(RespuestaEvaluacion::getId).orElse(null);

            RespuestaEvaluacion entidadAGuardar =
                    new RespuestaEvaluacion(
                            respuestaId,
                            evaluacionId,
                            subpruebaCodigo,
                            item.preguntaId(),
                            opcionIdAGuardar,
                            valorTextoAGuardar,
                            ordenEntrada,
                            ahora);

            respuestaRepository.guardar(entidadAGuardar);
        }

        long totalRespondidas =
                respuestaRepository.contarPorEvaluacionYSubprueba(evaluacionId, subpruebaCodigo);
        int totalPreguntas = preguntaRepository.listarPorSubprueba(subpruebaCodigo).size();

        return new AvanceSubpruebaResponseDto(
                evaluacionId,
                subpruebaCodigo,
                items.get(0).preguntaId(),
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
