package com.universidad.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.universidad.tests.application.service.DespachoCalculoSubpruebaService;
import com.universidad.tests.application.service.MotorSubpruebasService;
import com.universidad.tests.config.TemporizadorSubpruebasConfig;
import com.universidad.tests.domain.port.AutorizacionEvaluacionPort;
import com.universidad.tests.domain.port.EvaluadorSubpruebaPort;
import com.universidad.tests.domain.repository.EvaluacionRepository;
import com.universidad.tests.domain.repository.SolicitudCalculoRepository;
import com.universidad.tests.dto.ItemRespuestaDto;
import com.universidad.tests.dto.RegistrarRespuestaRequestDto;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "motor.temporizador.habilitado=false")
@AutoConfigureMockMvc
class AplicacionSubpruebasIntegrationTest {
    @Test
    void textoLibreSeGuardaYRecuperaSinAceptarTextoVacio() throws Exception {
        jdbc.update("DELETE FROM evaluacion_subprueba WHERE evaluacion_id=?", id);
        catalogo.agregarSubprueba(jdbc, id, "TEXTO", catalogo.texto());
        motor.iniciarSubprueba(id, "TEXTO");
        String pregunta =
                jdbc.queryForObject(
                        "SELECT id_pregunta::text FROM pregunta WHERE id_configuracion_subprueba=?",
                        String.class,
                        catalogo.texto());
        motor.registrarRespuesta(
                id,
                "TEXTO",
                new RegistrarRespuestaRequestDto(pregunta, null, " texto sintético ", 1));
        assertThat(motor.consultarPreguntas(id, "TEXTO").preguntas().get(0).respuestaActual())
                .isEqualTo(" texto sintético ");
        mvc.perform(
                        put(ruta("TEXTO", "respuestas"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"preguntaId\":" + pregunta + ",\"valorTexto\":\" \"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detalles[0].campo").value("valorTexto"));
        assertThat(motor.consultarPreguntas(id, "TEXTO").preguntas().get(0).respuestaActual())
                .isEqualTo(" texto sintético ");
    }

    @Test
    void e6VencidaPersisteCierreYSolicitudAunqueDevuelva409() throws Exception {
        iniciar();
        vencer();
        mvc.perform(get(ruta("VOC1", "preguntas")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SUBPRUEBA_NO_ACTIVA"));
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM solicitud_calculo_subprueba WHERE id_subprueba=?",
                                Long.class,
                                idSubprueba()))
                .isEqualTo(1);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT estado FROM evaluacion_subprueba WHERE id=?",
                                String.class,
                                idSubprueba()))
                .isEqualTo("CERRADA");
    }

    @Test
    void e5CierraLaAnteriorVencidaAntesDeValidarOrden() {
        catalogo.agregarSubprueba(jdbc, id, "VOC2", catalogo.voc2());
        iniciar();
        vencer();
        assertThat(motor.iniciarSubprueba(id, "VOC2").estado()).isEqualTo("ACTIVA");
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM solicitud_calculo_subprueba WHERE id_subprueba=?",
                                Long.class,
                                idSubprueba()))
                .isEqualTo(1);
    }

    @Test
    void noSeDuplicanRespuestasMedianteOrdenEntradaYLosErroresRespetanContrato() throws Exception {
        iniciar();
        mvc.perform(
                        put(ruta("VOC1", "respuestas"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"preguntaId\":"
                                                + catalogo.pregunta1()
                                                + ",\"opcion\":\"A\",\"ordenEntrada\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
        mvc.perform(
                        put(ruta("VOC1", "respuestas"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"respuestas\":[{\"preguntaId\":"
                                                + catalogo.pregunta1()
                                                + ",\"opcion\":\"Z\"}]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detalles[0].campo").value("opcion"));
        mvc.perform(
                        put(ruta("VOC1", "respuestas"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{malformado"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0].campo").value("cuerpo"));
        mvc.perform(get(ruta("INEXISTENTE", "preguntas")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("SUBPRUEBA_NO_ENCONTRADA"));
        mvc.perform(get("/api/v1/evaluaciones/INEXISTENTE/subpruebas/VOC1/preguntas"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("EVALUACION_NO_ENCONTRADA"));
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM respuesta_evaluacion WHERE evaluacion_id=?",
                                Long.class,
                                id))
                .isZero();
    }

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired MotorSubpruebasService motor;
    @Autowired EvaluacionRepository evaluaciones;
    @Autowired SolicitudCalculoRepository solicitudes;
    @Autowired DespachoCalculoSubpruebaService despacho;
    @Autowired Clock reloj;
    @MockBean AutorizacionEvaluacionPort autorizacion;
    @MockBean EvaluadorSubpruebaPort evaluador;
    private CatalogoMotorFixture catalogo;
    private String id;

    @BeforeEach
    void preparar() {
        id = "AP-" + UUID.randomUUID();
        catalogo = CatalogoMotorFixture.crear(jdbc);
        catalogo.evaluacion(jdbc, id, "VOC1", catalogo.voc1());
        when(evaluador.codigoTest()).thenReturn(catalogo.codigoTest());
    }

    @AfterEach
    void limpiar() {
        jdbc.update("DELETE FROM evaluacion WHERE id=?", id);
    }

    private String ruta(String codigo, String recurso) {
        return "/api/v1/evaluaciones/" + id + "/subpruebas/" + codigo + "/" + recurso;
    }

    private void iniciar() {
        motor.iniciarSubprueba(id, "VOC1");
    }

    private void vencer() {
        jdbc.update(
                "UPDATE evaluacion_subprueba SET fecha_inicio=now()-interval '301 seconds',fecha_fin_limite=now()-interval '1 second' WHERE evaluacion_id=? AND codigo='VOC1'",
                id);
    }

    private long idSubprueba() {
        return jdbc.queryForObject(
                "SELECT id FROM evaluacion_subprueba WHERE evaluacion_id=? AND codigo='VOC1'",
                Long.class,
                id);
    }

    private RegistrarRespuestaRequestDto respuesta(long pregunta, String opcion) {
        return new RegistrarRespuestaRequestDto(
                List.of(new ItemRespuestaDto(Long.toString(pregunta), opcion)));
    }

    @Test
    void instruccionesNoInicianRelojYSeUsaConfiguracionVersionada() throws Exception {
        assertThat(motor.consultarEvaluacion(id).subpruebaActiva()).isNull();
        assertThat(
                        jdbc.queryForObject(
                                "SELECT fecha_inicio FROM evaluacion_subprueba WHERE evaluacion_id=?",
                                java.sql.Timestamp.class,
                                id))
                .isNull();
        var primera = motor.iniciarSubprueba(id, "voc1");
        var segunda = motor.iniciarSubprueba(id, "VOC1");
        assertThat(primera.duracionSegundos())
                .isEqualTo(300); // El snapshot de la fixture contiene 999.
        assertThat(primera.finLimite()).isEqualTo(primera.inicio().plusSeconds(300));
        assertThat(segunda.inicio()).isEqualTo(primera.inicio());
        assertThat(segunda.finLimite()).isEqualTo(primera.finLimite());
    }

    @Test
    void preguntasSoloDeEdicionAplicadaConRecursosYSinClaves() throws Exception {
        iniciar();
        var resultado =
                mvc.perform(get(ruta("VOC1", "preguntas")))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.preguntas.length()").value(1))
                        .andExpect(jsonPath("$.preguntas[0].id").value(catalogo.pregunta1()))
                        .andExpect(
                                jsonPath("$.preguntas[0].recursoUrl")
                                        .value("/ficticio/pregunta.svg"))
                        .andExpect(jsonPath("$.preguntas[0].opciones[0].letra").value("A"))
                        .andExpect(
                                jsonPath("$.preguntas[0].opciones[0].recursoUrl")
                                        .value("/ficticio/opcion.svg"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertThat(resultado).doesNotContain("claveCorrecta", "clave_correcta", "esCorrecta");
    }

    @Test
    void edicionAjenaYOtraSubpruebaSeRechazanYElLoteRevierte() throws Exception {
        iniciar();
        for (long pregunta : List.of(catalogo.preguntaAjena(), catalogo.preguntaOtraVersion())) {
            String json =
                    "{\"respuestas\":[{\"preguntaId\":"
                            + catalogo.pregunta1()
                            + ",\"opcion\":\"A\"},{\"preguntaId\":"
                            + pregunta
                            + ",\"opcion\":\"A\"}]}";
            mvc.perform(
                            put(ruta("VOC1", "respuestas"))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.codigo").value("RESPUESTA_INVALIDA"))
                    .andExpect(jsonPath("$.detalles[0].campo").value("preguntaId"));
        }
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM respuesta_evaluacion WHERE evaluacion_id=?",
                                Long.class,
                                id))
                .isZero();
    }

    @Test
    void tresGuardadosSimultaneosSonIdempotentesYEditarActualiza() throws Exception {
        iniciar();
        var pool = Executors.newFixedThreadPool(3);
        try {
            Callable<Long> guardar =
                    () ->
                            motor.registrarRespuesta(
                                            id, "voc1", respuesta(catalogo.pregunta1(), "A"))
                                    .totalRespondidas();
            for (var futuro : pool.invokeAll(List.of(guardar, guardar, guardar)))
                assertThat(futuro.get()).isEqualTo(1);
        } finally {
            pool.shutdownNow();
        }
        motor.registrarRespuesta(id, "VOC1", respuesta(catalogo.pregunta1(), "B"));
        assertThat(motor.consultarPreguntas(id, "VOC1").preguntas().get(0).respuestaActual())
                .isEqualTo("B");
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM respuesta_evaluacion WHERE evaluacion_id=?",
                                Long.class,
                                id))
                .isEqualTo(1);
    }

    @Test
    void ordenImpideAdelantarYNoModificaEstado() throws Exception {
        catalogo.agregarSubprueba(jdbc, id, "VOC2", catalogo.voc2());
        mvc.perform(post(ruta("VOC2", "iniciar")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SUBPRUEBA_FUERA_DE_ORDEN"));
        iniciar();
        mvc.perform(post(ruta("VOC2", "iniciar")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("HAY_SUBPRUEBA_ACTIVA"));
        assertThat(
                        jdbc.queryForObject(
                                "SELECT estado FROM evaluacion_subprueba WHERE evaluacion_id=? AND codigo='VOC2'",
                                String.class,
                                id))
                .isEqualTo("PENDIENTE");
    }

    @Test
    void evaluacionFinalizadaNoAdmiteInicioConsultaDePreguntasNiRespuestas() throws Exception {
        jdbc.update("UPDATE evaluacion SET estado='FINALIZADA' WHERE id=?", id);
        mvc.perform(post(ruta("VOC1", "iniciar"))).andExpect(status().isConflict());
        mvc.perform(get(ruta("VOC1", "preguntas"))).andExpect(status().isConflict());
        mvc.perform(
                        put(ruta("VOC1", "respuestas"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"respuestas\":[]}"))
                .andExpect(status().isConflict());
    }

    @Test
    void temporizadorCierraSinPeticionesYEntregaCalculoUnaSolaVez() {
        iniciar();
        vencer();
        var temporizador = new TemporizadorSubpruebasConfig(evaluaciones, motor, despacho, reloj);
        temporizador.cerrarVencidas();
        var cierre =
                jdbc.queryForObject(
                        "SELECT fecha_cierre FROM evaluacion_subprueba WHERE id=?",
                        java.sql.Timestamp.class,
                        idSubprueba());
        temporizador.cerrarVencidas();
        motor.procesarVencimiento(id);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT estado FROM evaluacion_subprueba WHERE id=?",
                                String.class,
                                idSubprueba()))
                .isEqualTo("CERRADA");
        assertThat(
                        jdbc.queryForObject(
                                "SELECT fecha_cierre FROM evaluacion_subprueba WHERE id=?",
                                java.sql.Timestamp.class,
                                idSubprueba()))
                .isEqualTo(cierre);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM solicitud_calculo_subprueba WHERE id_subprueba=?",
                                Long.class,
                                idSubprueba()))
                .isEqualTo(1);
        temporizador.despacharCalculos();
        temporizador.despacharCalculos();
        verify(evaluador, times(1)).calcular(any());
        assertThat(
                        jdbc.queryForObject(
                                "SELECT procesada_en FROM solicitud_calculo_subprueba WHERE id_subprueba=?",
                                java.sql.Timestamp.class,
                                idSubprueba()))
                .isNotNull();
    }

    @Test
    void falloDelEvaluadorNoPierdeCierreNiSolicitudYSePuedeReintentar() {
        iniciar();
        vencer();
        motor.procesarVencimiento(id);
        doThrow(new IllegalStateException("Fallo sintético"))
                .doNothing()
                .when(evaluador)
                .calcular(any());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> despacho.procesar(idSubprueba()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT procesada_en FROM solicitud_calculo_subprueba WHERE id_subprueba=?",
                                java.sql.Timestamp.class,
                                idSubprueba()))
                .isNull();
        assertThat(
                        jdbc.queryForObject(
                                "SELECT estado FROM evaluacion_subprueba WHERE id=?",
                                String.class,
                                idSubprueba()))
                .isEqualTo("CERRADA");
        despacho.procesar(idSubprueba());
        despacho.procesar(idSubprueba());
        verify(evaluador, times(2)).calcular(any());
    }

    @Test
    void evaluadorAusenteConservaSolicitudPendiente() {
        iniciar();
        vencer();
        motor.procesarVencimiento(id);
        when(evaluador.codigoTest()).thenReturn("OTRO_TEST");
        despacho.procesar(idSubprueba());
        verify(evaluador, never()).calcular(any());
        assertThat(
                        jdbc.queryForObject(
                                "SELECT procesada_en FROM solicitud_calculo_subprueba WHERE id_subprueba=?",
                                java.sql.Timestamp.class,
                                idSubprueba()))
                .isNull();
    }

    @Test
    void cierresConcurrentesProducenUnaSolaSolicitudYPermitenLaSiguiente() throws Exception {
        catalogo.agregarSubprueba(jdbc, id, "VOC2", catalogo.voc2());
        iniciar();
        vencer();
        var pool = Executors.newFixedThreadPool(3);
        try {
            Callable<Void> cerrar =
                    () -> {
                        motor.procesarVencimiento(id);
                        return null;
                    };
            for (var futuro : pool.invokeAll(List.of(cerrar, cerrar, cerrar))) futuro.get();
        } finally {
            pool.shutdownNow();
        }
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM solicitud_calculo_subprueba WHERE id_subprueba=?",
                                Long.class,
                                idSubprueba()))
                .isEqualTo(1);
        assertThat(motor.iniciarSubprueba(id, "VOC2").estado()).isEqualTo("ACTIVA");
    }
}
