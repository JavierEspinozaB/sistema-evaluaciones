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
import com.universidad.tests.domain.model.PreguntaAplicacion;
import com.universidad.tests.domain.model.RespuestaEvaluacion;
import com.universidad.tests.domain.model.TipoRespuestaAplicacion;
import com.universidad.tests.domain.repository.EvaluacionRepository;
import com.universidad.tests.domain.repository.PreguntaRepositoryAplicacion;
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
            SubpruebaNoActivaException.class,
            com.universidad.tests.domain.exception.SubpruebaFueraDeOrdenException.class
        })
public class MotorSubpruebasService {

    private final EvaluacionRepository evaluacionRepository;
    private final PreguntaRepositoryAplicacion preguntaRepository;
    private final RespuestaRepository respuestaRepository;
    private final MotorSubpruebasMapper mapper;
    private final Clock clock;
    private final com.universidad.tests.domain.port.AutorizacionEvaluacionPort autorizacion;
    private final com.universidad.tests.domain.repository.SolicitudCalculoRepository solicitudes;

    public MotorSubpruebasService(
            EvaluacionRepository evaluacionRepository,
            PreguntaRepositoryAplicacion preguntaRepository,
            RespuestaRepository respuestaRepository,
            MotorSubpruebasMapper mapper,
            Clock clock,
            com.universidad.tests.domain.port.AutorizacionEvaluacionPort autorizacion,
            com.universidad.tests.domain.repository.SolicitudCalculoRepository solicitudes) {
        this.evaluacionRepository = evaluacionRepository;
        this.preguntaRepository = preguntaRepository;
        this.respuestaRepository = respuestaRepository;
        this.mapper = mapper;
        this.clock = clock;
        this.autorizacion = autorizacion;
        this.solicitudes = solicitudes;
    }

    public EvaluacionResponseDto consultarEvaluacion(String evaluacionId) {
        autorizacion.exigirAcceso(evaluacionId);
        Evaluacion evaluacion = obtenerEvaluacionOError(evaluacionId);
        Instant ahora = clock.instant().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        cerrarVencidas(evaluacion, ahora);
        return mapper.toEvaluacionDto(evaluacion, ahora);
    }

    private void cerrarVencidas(Evaluacion evaluacion, Instant ahora) {
        for (EvaluacionSubprueba sub : evaluacion.getSubpruebas()) {
            if (sub.getEstado() == EstadoSubprueba.ACTIVA && sub.haExpirado(ahora)) {
                sub.cerrar(ahora);
                evaluacionRepository.guardarSubprueba(sub);
                solicitudes.registrar(sub.getId());
            }
        }
    }

    public SubpruebaResponseDto iniciarSubprueba(String evaluacionId, String subpruebaCodigo) {
        autorizacion.exigirAcceso(evaluacionId);
        Evaluacion evaluacion = obtenerEvaluacionOError(evaluacionId);
        Instant ahora = clock.instant().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        if (!"EN_CURSO".equals(evaluacion.getEstado()))
            throw new SubpruebaCerradaException("La evaluación no está en curso.");
        cerrarVencidas(evaluacion, ahora);
        EvaluacionSubprueba subprueba = evaluacion.buscarSubprueba(subpruebaCodigo);
        if (subprueba.getEstado() == EstadoSubprueba.CERRADA) {
            throw new SubpruebaCerradaException("La subprueba ya está cerrada.");
        }
        if (evaluacion.getSubpruebas().stream()
                .anyMatch(
                        s ->
                                s.getEstado() == EstadoSubprueba.ACTIVA
                                        && !s.getCodigo().equalsIgnoreCase(subpruebaCodigo))) {
            throw new com.universidad.tests.domain.exception.SubpruebaFueraDeOrdenException(
                    "HAY_SUBPRUEBA_ACTIVA", "Ya existe otra subprueba activa.");
        }
        evaluacion.validarOrdenSubprueba(subpruebaCodigo);
        if (subprueba.esFluidezVerbal()
                && (subprueba.getLetraInicial() == null || subprueba.getLetraInicial().isBlank())) {
            throw new com.universidad.tests.domain.exception.LetraNoConfiguradaException();
        }
        subprueba.iniciar(ahora);
        evaluacionRepository.guardarSubprueba(subprueba);

        return mapper.toSubpruebaDto(subprueba, ahora);
    }

    public PreguntasSubpruebaResponseDto consultarPreguntas(
            String evaluacionId, String subpruebaCodigo) {
        autorizacion.exigirAcceso(evaluacionId);
        Evaluacion evaluacion = obtenerEvaluacionOError(evaluacionId);
        Instant ahora = clock.instant().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        EvaluacionSubprueba subprueba = evaluacion.buscarSubprueba(subpruebaCodigo);
        if (!"EN_CURSO".equals(evaluacion.getEstado()))
            throw new SubpruebaNoActivaException("La evaluación no está en curso.");

        if (subprueba.getEstado() == EstadoSubprueba.ACTIVA && subprueba.haExpirado(ahora)) {
            subprueba.cerrar(ahora);
            evaluacionRepository.guardarSubprueba(subprueba);
            solicitudes.registrar(subprueba.getId());
            throw new SubpruebaNoActivaException(
                    "El tiempo de la subprueba " + subpruebaCodigo + " ha finalizado.");
        }

        if (subprueba.getEstado() != EstadoSubprueba.ACTIVA) {
            throw new SubpruebaNoActivaException(
                    "La subprueba " + subpruebaCodigo + " no se encuentra activa.");
        }

        List<PreguntaAplicacion> preguntas =
                preguntaRepository.listarPorConfiguracion(subprueba.getConfiguracionId());
        List<RespuestaEvaluacion> respuestas =
                respuestaRepository.listarPorEvaluacionYSubprueba(evaluacionId, subpruebaCodigo);

        return mapper.toPreguntasSubpruebaDto(subprueba, preguntas, respuestas, ahora);
    }

    public AvanceSubpruebaResponseDto registrarRespuesta(
            String evaluacionId, String subpruebaCodigo, RegistrarRespuestaRequestDto request) {
        autorizacion.exigirAcceso(evaluacionId);
        Evaluacion evaluacion = obtenerEvaluacionOError(evaluacionId);
        Instant ahora = clock.instant().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        EvaluacionSubprueba subprueba = evaluacion.buscarSubprueba(subpruebaCodigo);
        if (!"EN_CURSO".equals(evaluacion.getEstado()))
            throw new SubpruebaNoActivaException("La evaluación no está en curso.");

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
            solicitudes.registrar(subprueba.getId());
            throw new TiempoAgotadoException(
                    "El tiempo límite para la subprueba " + subpruebaCodigo + " ha finalizado.");
        }

        if (subprueba.esFluidezVerbal()) {
            return registrarPalabras(evaluacionId, subprueba, request, ahora);
        }
        if (request != null && request.palabras() != null) {
            throw new SolicitudInvalidaException(
                    "Esta subprueba requiere respuestas.",
                    "palabras",
                    "Solo se admiten palabras para FV.");
        }
        List<ItemRespuestaDto> items =
                request != null ? request.obtenerListaRespuestas() : List.of();
        if (items.isEmpty()) {
            throw new SolicitudInvalidaException(
                    "El identificador de pregunta es obligatorio.",
                    "preguntaId",
                    "preguntaId es obligatorio");
        }

        java.util.Set<String> preguntasIncluidas = new java.util.HashSet<>();
        for (ItemRespuestaDto item : items) {
            if (item == null || item.preguntaId() == null || item.preguntaId().isBlank()) {
                throw new SolicitudInvalidaException(
                        "El identificador de pregunta es obligatorio.",
                        "preguntaId",
                        "preguntaId es obligatorio");
            }

            if (!preguntasIncluidas.add(item.preguntaId())
                    || (item.ordenEntrada() != null && item.ordenEntrada() != 1)) {
                throw new SolicitudInvalidaException(
                        "Cada pregunta admite una sola respuesta por solicitud.",
                        "respuestas",
                        "No repita preguntas ni use ordenEntrada distinto de 1.");
            }
            PreguntaAplicacion pregunta =
                    preguntaRepository
                            .buscarPorIdYConfiguracion(
                                    item.preguntaId(), subprueba.getConfiguracionId())
                            .orElseThrow(
                                    () ->
                                            new RespuestaInvalidaException(
                                                    "preguntaId",
                                                    "Pregunta no perteneciente a la configuración aplicada: "
                                                            + item.preguntaId()));

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

            if (pregunta.getTipoRespuesta() == TipoRespuestaAplicacion.SELECCION) {
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

            if (pregunta.getTipoRespuesta() != TipoRespuestaAplicacion.SELECCION
                    && (valorTextoAGuardar == null || valorTextoAGuardar.isBlank()))
                throw new RespuestaInvalidaException("valorTexto", "El texto es obligatorio.");
            int ordenEntrada = 1;

            Optional<RespuestaEvaluacion> existente =
                    respuestaRepository.buscarRespuesta(
                            evaluacionId, subprueba.getCodigo(), item.preguntaId(), ordenEntrada);

            Long respuestaId = existente.map(RespuestaEvaluacion::getId).orElse(null);

            RespuestaEvaluacion entidadAGuardar =
                    new RespuestaEvaluacion(
                            respuestaId,
                            evaluacionId,
                            subprueba.getCodigo(),
                            item.preguntaId(),
                            opcionIdAGuardar,
                            valorTextoAGuardar,
                            ordenEntrada,
                            ahora);

            respuestaRepository.guardar(entidadAGuardar);
        }

        long totalRespondidas =
                respuestaRepository.contarPorEvaluacionYSubprueba(evaluacionId, subpruebaCodigo);
        int totalPreguntas =
                preguntaRepository.listarPorConfiguracion(subprueba.getConfiguracionId()).size();

        return new AvanceSubpruebaResponseDto(
                evaluacionId,
                subpruebaCodigo,
                items.get(0).preguntaId(),
                totalRespondidas,
                totalPreguntas,
                true,
                subprueba.getCodigo(),
                items.size(),
                subprueba.calcularSegundosRestantes(ahora));
    }

    private AvanceSubpruebaResponseDto registrarPalabras(
            String evaluacionId,
            EvaluacionSubprueba subprueba,
            RegistrarRespuestaRequestDto request,
            Instant ahora) {
        String codigo = subprueba.getCodigo();
        if (request == null
                || request.palabras() == null
                || request.palabras().isEmpty()
                || !request.obtenerListaRespuestas().isEmpty()) {
            throw new SolicitudInvalidaException(
                    "FV requiere una lista de palabras.",
                    "palabras",
                    "Envíe palabras con ordenEntrada y texto.");
        }
        java.util.Set<Integer> ordenes = new java.util.HashSet<>();
        for (com.universidad.tests.dto.PalabraFvDto palabra : request.palabras()) {
            if (palabra == null
                    || palabra.ordenEntrada() == null
                    || palabra.ordenEntrada() < 1
                    || palabra.ordenEntrada() > subprueba.getMaximoEntradas()
                    || !ordenes.add(palabra.ordenEntrada())) {
                throw new SolicitudInvalidaException(
                        "Orden de entrada inválido.",
                        "ordenEntrada",
                        "El orden debe ser único y estar entre 1 y "
                                + subprueba.getMaximoEntradas());
            }
            if (palabra.texto() == null || palabra.texto().isBlank()) {
                throw new SolicitudInvalidaException(
                        "Texto de palabra vacío.", "texto", "El texto es obligatorio.");
            }
        }
        for (com.universidad.tests.dto.PalabraFvDto palabra : request.palabras()) {
            Long id =
                    respuestaRepository
                            .buscarRespuesta(evaluacionId, codigo, null, palabra.ordenEntrada())
                            .map(RespuestaEvaluacion::getId)
                            .orElse(null);
            respuestaRepository.guardar(
                    new RespuestaEvaluacion(
                            id,
                            evaluacionId,
                            codigo,
                            null,
                            null,
                            palabra.texto(),
                            palabra.ordenEntrada(),
                            ahora));
        }
        return new AvanceSubpruebaResponseDto(
                evaluacionId,
                codigo,
                null,
                respuestaRepository.contarPorEvaluacionYSubprueba(evaluacionId, codigo),
                0,
                true,
                codigo,
                request.palabras().size(),
                subprueba.calcularSegundosRestantes(ahora));
    }

    /**
     * Entrada interna del temporizador: no se publica como endpoint ni requiere sesión de usuario.
     */
    public void procesarVencimiento(String evaluacionId) {
        Evaluacion evaluacion = obtenerEvaluacionOError(evaluacionId);
        cerrarVencidas(
                evaluacion, clock.instant().truncatedTo(java.time.temporal.ChronoUnit.MICROS));
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
