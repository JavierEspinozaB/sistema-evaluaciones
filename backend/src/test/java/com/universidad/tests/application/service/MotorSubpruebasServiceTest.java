package com.universidad.tests.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.universidad.tests.domain.exception.RecursoNoEncontradoException;
import com.universidad.tests.domain.exception.RespuestaInvalidaException;
import com.universidad.tests.domain.exception.SolicitudInvalidaException;
import com.universidad.tests.domain.exception.SubpruebaCerradaException;
import com.universidad.tests.domain.exception.SubpruebaFueraDeOrdenException;
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
import com.universidad.tests.dto.PreguntasSubpruebaResponseDto;
import com.universidad.tests.dto.RegistrarRespuestaRequestDto;
import com.universidad.tests.dto.SubpruebaResponseDto;
import com.universidad.tests.mapper.MotorSubpruebasMapper;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MotorSubpruebasServiceTest {

    private EvaluacionRepository evaluacionRepository;
    private PreguntaRepository preguntaRepository;
    private RespuestaRepository respuestaRepository;
    private MotorSubpruebasMapper mapper;
    private Clock clock;
    private Instant baseTime;
    private MotorSubpruebasService service;

    @BeforeEach
    void setUp() {
        evaluacionRepository = mock(EvaluacionRepository.class);
        preguntaRepository = mock(PreguntaRepository.class);
        respuestaRepository = mock(RespuestaRepository.class);
        mapper = new MotorSubpruebasMapper();
        baseTime = Instant.parse("2026-11-06T10:00:00Z");
        clock = Clock.fixed(baseTime, ZoneId.of("UTC"));
        service =
                new MotorSubpruebasService(
                        evaluacionRepository,
                        preguntaRepository,
                        respuestaRepository,
                        mapper,
                        clock);
    }

    private Evaluacion crearEvaluacionMock(EstadoSubprueba estadoVoc1, EstadoSubprueba estadoVoc2) {
        EvaluacionSubprueba voc1 =
                new EvaluacionSubprueba(
                        1L,
                        "EV-01",
                        "VOC1",
                        "Vocabulario 1",
                        1,
                        300,
                        estadoVoc1,
                        "Instrucciones VOC1",
                        true,
                        estadoVoc1 == EstadoSubprueba.ACTIVA ? baseTime : null,
                        estadoVoc1 == EstadoSubprueba.ACTIVA
                                ? baseTime.plus(Duration.ofSeconds(300))
                                : null,
                        estadoVoc1 == EstadoSubprueba.CERRADA ? baseTime : null);

        EvaluacionSubprueba voc2 =
                new EvaluacionSubprueba(
                        2L,
                        "EV-01",
                        "VOC2",
                        "Vocabulario 2",
                        2,
                        360,
                        estadoVoc2,
                        "Instrucciones VOC2",
                        true,
                        null,
                        null,
                        null);

        return new Evaluacion("EV-01", "SUJETO-01", "EN_PROGRESO", List.of(voc1, voc2));
    }

    @Test
    @DisplayName("TC-GEN-03-009: Iniciar subprueba fija inicio, finLimite y segundosRestantes")
    void tc009_iniciarSubpruebaFijaTiempo() {
        Evaluacion evaluacion =
                crearEvaluacionMock(EstadoSubprueba.PENDIENTE, EstadoSubprueba.PENDIENTE);
        when(evaluacionRepository.buscarPorId("EV-01")).thenReturn(Optional.of(evaluacion));

        SubpruebaResponseDto dto = service.iniciarSubprueba("EV-01", "VOC1");

        assertThat(dto.estado()).isEqualTo("ACTIVA");
        assertThat(dto.inicio()).isEqualTo(baseTime);
        assertThat(dto.finLimite()).isEqualTo(baseTime.plus(Duration.ofSeconds(300)));
        assertThat(dto.segundosRestantes()).isEqualTo(300L);
        verify(evaluacionRepository).guardarSubprueba(any());
    }

    @Test
    @DisplayName(
            "TC-GEN-03-010: Repetir inicio sobre subprueba activa es idempotente y no reinicia tiempo")
    void tc010_repetirInicioNoReiniciaTiempo() {
        Evaluacion evaluacion =
                crearEvaluacionMock(EstadoSubprueba.ACTIVA, EstadoSubprueba.PENDIENTE);
        when(evaluacionRepository.buscarPorId("EV-01")).thenReturn(Optional.of(evaluacion));

        SubpruebaResponseDto dto = service.iniciarSubprueba("EV-01", "VOC1");

        assertThat(dto.estado()).isEqualTo("ACTIVA");
        assertThat(dto.inicio()).isEqualTo(baseTime);
        assertThat(dto.finLimite()).isEqualTo(baseTime.plus(Duration.ofSeconds(300)));
    }

    @Test
    @DisplayName(
            "TC-GEN-03-011 y TC-GEN-03-012: Consultar preguntas sin claves y con respuesta previa")
    void tc011_tc012_consultarPreguntasSinClaves() {
        Evaluacion evaluacion =
                crearEvaluacionMock(EstadoSubprueba.ACTIVA, EstadoSubprueba.PENDIENTE);
        when(evaluacionRepository.buscarPorId("EV-01")).thenReturn(Optional.of(evaluacion));

        Pregunta p1 =
                new Pregunta(
                        "P-01",
                        "VOC1",
                        1,
                        "Pregunta 1",
                        null,
                        TipoRespuesta.SELECCION,
                        "OPT-A",
                        1,
                        List.of(
                                new Opcion("OPT-A", "A", "Opción A", 1),
                                new Opcion("OPT-B", "B", "Opción B", 2)));

        when(preguntaRepository.listarPorSubprueba("VOC1")).thenReturn(List.of(p1));
        when(respuestaRepository.listarPorEvaluacionYSubprueba("EV-01", "VOC1"))
                .thenReturn(
                        List.of(
                                new RespuestaEvaluacion(
                                        1L, "EV-01", "VOC1", "P-01", "OPT-B", null, 1, baseTime)));

        PreguntasSubpruebaResponseDto result = service.consultarPreguntas("EV-01", "VOC1");

        assertThat(result.codigo()).isEqualTo("VOC1");
        assertThat(result.segundosRestantes()).isEqualTo(300L);
        assertThat(result.preguntas()).hasSize(1);
        assertThat(result.preguntas().get(0).respuestaActual()).isEqualTo("OPT-B");
        // Verificar que no hay campo claveCorrecta expuesto en el DTO
        assertThat(result.preguntas().get(0).opciones()).hasSize(2);
    }

    @Test
    @DisplayName("TC-GEN-03-013 y TC-GEN-03-014: Registrar respuesta es idempotente")
    void tc013_tc014_registrarRespuestaIdempotente() {
        Evaluacion evaluacion =
                crearEvaluacionMock(EstadoSubprueba.ACTIVA, EstadoSubprueba.PENDIENTE);
        when(evaluacionRepository.buscarPorId("EV-01")).thenReturn(Optional.of(evaluacion));

        Pregunta p1 =
                new Pregunta(
                        "P-01",
                        "VOC1",
                        1,
                        "Pregunta 1",
                        null,
                        TipoRespuesta.SELECCION,
                        "OPT-A",
                        1,
                        List.of(new Opcion("OPT-A", "A", "Opción A", 1)));
        when(preguntaRepository.buscarPorId("P-01")).thenReturn(Optional.of(p1));
        when(respuestaRepository.contarPorEvaluacionYSubprueba("EV-01", "VOC1")).thenReturn(1L);
        when(preguntaRepository.listarPorSubprueba("VOC1")).thenReturn(List.of(p1));

        RegistrarRespuestaRequestDto request =
                new RegistrarRespuestaRequestDto("P-01", "OPT-A", null, 1);

        AvanceSubpruebaResponseDto avance = service.registrarRespuesta("EV-01", "VOC1", request);

        assertThat(avance.guardado()).isTrue();
        assertThat(avance.totalRespondidas()).isEqualTo(1L);
        verify(respuestaRepository).guardar(any());
    }

    @Test
    @DisplayName("TC-GEN-03-015: Respuesta fuera de plazo es rechazada con TIEMPO_AGOTADO")
    void tc015_respuestaFueraDePlazoRechazada() {
        Evaluacion evaluacion =
                crearEvaluacionMock(EstadoSubprueba.ACTIVA, EstadoSubprueba.PENDIENTE);
        when(evaluacionRepository.buscarPorId("EV-01")).thenReturn(Optional.of(evaluacion));

        // Avanzar el reloj más allá de los 300 segundos
        Clock clockExpirado = Clock.fixed(baseTime.plus(Duration.ofSeconds(305)), ZoneId.of("UTC"));
        MotorSubpruebasService serviceExpirado =
                new MotorSubpruebasService(
                        evaluacionRepository,
                        preguntaRepository,
                        respuestaRepository,
                        mapper,
                        clockExpirado);

        RegistrarRespuestaRequestDto request =
                new RegistrarRespuestaRequestDto("P-01", "OPT-A", null, 1);

        assertThatThrownBy(() -> serviceExpirado.registrarRespuesta("EV-01", "VOC1", request))
                .isInstanceOf(TiempoAgotadoException.class);

        verify(evaluacionRepository).guardarSubprueba(any());
    }

    @Test
    @DisplayName(
            "TC-GEN-03-016: Iniciar subprueba fuera de orden lanza SubpruebaFueraDeOrdenException")
    void tc016_iniciarFueraDeOrdenRechazado() {
        Evaluacion evaluacion =
                crearEvaluacionMock(EstadoSubprueba.PENDIENTE, EstadoSubprueba.PENDIENTE);
        when(evaluacionRepository.buscarPorId("EV-01")).thenReturn(Optional.of(evaluacion));

        assertThatThrownBy(() -> service.iniciarSubprueba("EV-01", "VOC2"))
                .isInstanceOf(SubpruebaFueraDeOrdenException.class);
    }

    @Test
    @DisplayName(
            "TC-GEN-03-017: Modificar respuesta de subprueba cerrada lanza SubpruebaNoActivaException")
    void tc017_modificarRespuestaSubpruebaCerradaRechazado() {
        Evaluacion evaluacion =
                crearEvaluacionMock(EstadoSubprueba.CERRADA, EstadoSubprueba.PENDIENTE);
        when(evaluacionRepository.buscarPorId("EV-01")).thenReturn(Optional.of(evaluacion));

        RegistrarRespuestaRequestDto request =
                new RegistrarRespuestaRequestDto("P-01", "OPT-A", null, 1);

        assertThatThrownBy(() -> service.registrarRespuesta("EV-01", "VOC1", request))
                .isInstanceOf(SubpruebaNoActivaException.class);
    }

    @Test
    @DisplayName(
            "TC-GEN-03-018: Pregunta de otra subprueba es rechazada con RespuestaInvalidaException")
    void tc018_preguntaDeOtraSubpruebaRechazada() {
        Evaluacion evaluacion =
                crearEvaluacionMock(EstadoSubprueba.ACTIVA, EstadoSubprueba.PENDIENTE);
        when(evaluacionRepository.buscarPorId("EV-01")).thenReturn(Optional.of(evaluacion));

        Pregunta pVoc2 =
                new Pregunta(
                        "P-VOC2-01",
                        "VOC2",
                        1,
                        "Pregunta VOC2",
                        null,
                        TipoRespuesta.SELECCION,
                        "OPT-A",
                        1,
                        List.of());
        when(preguntaRepository.buscarPorId("P-VOC2-01")).thenReturn(Optional.of(pVoc2));

        RegistrarRespuestaRequestDto request =
                new RegistrarRespuestaRequestDto("P-VOC2-01", "OPT-A", null, 1);

        assertThatThrownBy(() -> service.registrarRespuesta("EV-01", "VOC1", request))
                .isInstanceOf(RespuestaInvalidaException.class);
    }

    @Test
    @DisplayName("TC-GEN-03-019: Iniciar subprueba cerrada lanza SubpruebaCerradaException")
    void tc019_iniciarSubpruebaCerradaRechazado() {
        Evaluacion evaluacion =
                crearEvaluacionMock(EstadoSubprueba.CERRADA, EstadoSubprueba.PENDIENTE);
        when(evaluacionRepository.buscarPorId("EV-01")).thenReturn(Optional.of(evaluacion));

        assertThatThrownBy(() -> service.iniciarSubprueba("EV-01", "VOC1"))
                .isInstanceOf(SubpruebaCerradaException.class);
    }

    @Test
    @DisplayName(
            "TC-GEN-03-020: Evaluación o subprueba inexistente lanza RecursoNoEncontradoException")
    void tc020_evaluacionOSubpruebaInexistente() {
        when(evaluacionRepository.buscarPorId("EV-INEXISTENTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultarEvaluacion("EV-INEXISTENTE"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("TC-GEN-03-021: Opción inválida o preguntaId nulo lanza excepción correspondiente")
    void tc021_opcionInvalidaOCuerpoInvalido() {
        Evaluacion evaluacion =
                crearEvaluacionMock(EstadoSubprueba.ACTIVA, EstadoSubprueba.PENDIENTE);
        when(evaluacionRepository.buscarPorId("EV-01")).thenReturn(Optional.of(evaluacion));

        // preguntaId nulo
        assertThatThrownBy(
                        () ->
                                service.registrarRespuesta(
                                        "EV-01",
                                        "VOC1",
                                        new RegistrarRespuestaRequestDto(null, "OPT-A", null, 1)))
                .isInstanceOf(SolicitudInvalidaException.class);

        // Opción ajena
        Pregunta p1 =
                new Pregunta(
                        "P-01",
                        "VOC1",
                        1,
                        "Pregunta 1",
                        null,
                        TipoRespuesta.SELECCION,
                        "OPT-A",
                        1,
                        List.of(new Opcion("OPT-A", "A", "Opción A", 1)));
        when(preguntaRepository.buscarPorId("P-01")).thenReturn(Optional.of(p1));

        assertThatThrownBy(
                        () ->
                                service.registrarRespuesta(
                                        "EV-01",
                                        "VOC1",
                                        new RegistrarRespuestaRequestDto(
                                                "P-01", "OPT-AJENA", null, 1)))
                .isInstanceOf(RespuestaInvalidaException.class);
    }
}
