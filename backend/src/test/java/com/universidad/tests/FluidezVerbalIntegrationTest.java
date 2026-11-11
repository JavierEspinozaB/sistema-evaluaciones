package com.universidad.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.universidad.tests.application.service.MotorSubpruebasService;
import com.universidad.tests.dto.PalabraFvDto;
import com.universidad.tests.dto.RegistrarRespuestaRequestDto;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/** Regresión BUG-03 con PostgreSQL real, Flyway, HTTP y transacciones independientes. */
@SpringBootTest(properties = "motor.temporizador.habilitado=false")
@AutoConfigureMockMvc
class FluidezVerbalIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired MotorSubpruebasService motor;
    private String id;
    private CatalogoMotorFixture catalogo;

    @org.springframework.boot.test.mock.mockito.MockBean
    com.universidad.tests.domain.port.AutorizacionEvaluacionPort autorizacion;

    @BeforeEach
    void preparar() {
        id = "FV-TEST-" + UUID.randomUUID();
        catalogo = CatalogoMotorFixture.crear(jdbc);
        catalogo.evaluacion(jdbc, id, "FV", catalogo.fv());
    }

    @AfterEach
    void limpiar() {
        jdbc.update("DELETE FROM evaluacion WHERE id = ?", id);
    }

    private String ruta(String sufijo) {
        return "/api/v1/evaluaciones/" + id + "/subpruebas/FV/" + sufijo;
    }

    private void iniciar() throws Exception {
        mvc.perform(post(ruta("iniciar")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.letraInicial").value("P"));
    }

    private org.springframework.test.web.servlet.ResultActions enviar(String json)
            throws Exception {
        return mvc.perform(
                put(ruta("respuestas")).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void letraAsignadaSeConservaEnReinicioYE4IncluyeMetadatos() throws Exception {
        iniciar();
        var primera = motor.iniciarSubprueba(id, "FV");
        var segunda = motor.iniciarSubprueba(id, "fv");
        assertThat(segunda.inicio()).isEqualTo(primera.inicio());
        assertThat(segunda.finLimite()).isEqualTo(primera.finLimite());
        assertThat(segunda.letraInicial()).isEqualTo("P");
        mvc.perform(get("/api/v1/evaluaciones/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testId").value(Long.toString(catalogo.testId())))
                .andExpect(jsonPath("$.periodoId").value("1"))
                .andExpect(
                        jsonPath("$.versionFormularioId")
                                .value(Long.toString(catalogo.versionId())))
                .andExpect(jsonPath("$.fechaInicio").isNotEmpty())
                .andExpect(jsonPath("$.subpruebaActiva.codigo").value("FV"));
    }

    @Test
    void sinLetraNoIniciaNiConsumeTiempo() throws Exception {
        jdbc.update(
                "UPDATE evaluacion_subprueba SET letra_inicial = null WHERE evaluacion_id = ?", id);
        mvc.perform(post(ruta("iniciar")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("LETRA_NO_CONFIGURADA"));
        assertThat(
                        jdbc.queryForObject(
                                "SELECT estado FROM evaluacion_subprueba WHERE evaluacion_id = ?",
                                String.class,
                                id))
                .isEqualTo("PENDIENTE");
    }

    @Test
    void palabrasSinPreguntasSeRecuperanConOrdenYTextoOriginal() throws Exception {
        iniciar();
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM pregunta WHERE id_configuracion_subprueba = ?",
                                Long.class,
                                catalogo.fv()))
                .isZero();
        enviar(
                        "{\"palabras\":[{\"ordenEntrada\":3,\"texto\":\" montaña \"},{\"ordenEntrada\":1,\"texto\":\"mesa\"}]}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("FV"))
                .andExpect(jsonPath("$.registradas").value(2))
                .andExpect(jsonPath("$.totalRespondidas").value(2))
                .andExpect(jsonPath("$.segundosRestantes").isNumber());
        enviar("{\"palabras\":[{\"ordenEntrada\":1,\"texto\":\"mano\"}]}")
                .andExpect(status().isOk());
        mvc.perform(get(ruta("preguntas")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preguntas").isEmpty())
                .andExpect(jsonPath("$.letraInicial").value("P"))
                .andExpect(jsonPath("$.palabras.length()").value(2))
                .andExpect(jsonPath("$.palabras[0].ordenEntrada").value(1))
                .andExpect(jsonPath("$.palabras[0].texto").value("mano"))
                .andExpect(jsonPath("$.palabras[1].ordenEntrada").value(3))
                .andExpect(jsonPath("$.palabras[1].texto").value(" montaña "));
    }

    @Test
    void reenviosSimultaneosNoDuplicanNiFallan() throws Exception {
        iniciar();
        var peticion =
                new RegistrarRespuestaRequestDto(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        List.of(new PalabraFvDto(1, "mesa"), new PalabraFvDto(2, "mano")));
        var pool = Executors.newFixedThreadPool(3);
        try {
            List<Callable<Long>> tareas =
                    IntStream.range(0, 3)
                            .mapToObj(
                                    i ->
                                            (Callable<Long>)
                                                    () ->
                                                            motor.registrarRespuesta(
                                                                            id, "FV", peticion)
                                                                    .totalRespondidas())
                            .toList();
            for (var resultado : pool.invokeAll(tareas)) assertThat(resultado.get()).isEqualTo(2);
        } finally {
            pool.shutdownNow();
        }
        assertThat(motor.consultarPreguntas(id, "FV").palabras()).hasSize(2);
    }

    @Test
    void validaTodoElLoteAntesDeGuardar() throws Exception {
        iniciar();
        for (String json :
                List.of(
                        "{\"palabras\":[{\"ordenEntrada\":1,\"texto\":\"mesa\"},{\"ordenEntrada\":2,\"texto\":\"  \"}]}",
                        "{\"palabras\":[{\"ordenEntrada\":1,\"texto\":\"mesa\"},{\"ordenEntrada\":1,\"texto\":\"mano\"}]}",
                        "{\"palabras\":[{\"ordenEntrada\":76,\"texto\":\"mesa\"}]}",
                        "{\"palabras\":[null]}",
                        "{\"palabras\":[]}")) {
            enviar(json)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"))
                    .andExpect(jsonPath("$.detalles[0].campo").exists());
        }
        assertThat(motor.consultarPreguntas(id, "FV").palabras()).isEmpty();
    }

    @Test
    void admite75EntradasYElReenvioNoAumentaTotal() throws Exception {
        iniciar();
        var palabras =
                IntStream.rangeClosed(1, 75)
                        .mapToObj(i -> new PalabraFvDto(i, "ficticia-" + i))
                        .toList();
        var request =
                new RegistrarRespuestaRequestDto(null, null, null, null, null, null, palabras);
        assertThat(motor.registrarRespuesta(id, "FV", request).totalRespondidas()).isEqualTo(75);
        assertThat(motor.registrarRespuesta(id, "FV", request).totalRespondidas()).isEqualTo(75);
        assertThat(motor.consultarPreguntas(id, "FV").palabras()).hasSize(75);
    }

    @Test
    void vencimientoPersisteCierreYNoGuardaPalabrasTardias() throws Exception {
        iniciar();
        jdbc.update(
                "UPDATE evaluacion_subprueba SET fecha_inicio = now() - interval '301 seconds', fecha_fin_limite = now() - interval '1 second' WHERE evaluacion_id = ?",
                id);
        enviar("{\"palabras\":[{\"ordenEntrada\":1,\"texto\":\"tardia\"}]}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TIEMPO_AGOTADO"));
        assertThat(
                        jdbc.queryForObject(
                                "SELECT estado FROM evaluacion_subprueba WHERE evaluacion_id = ?",
                                String.class,
                                id))
                .isEqualTo("CERRADA");
        var cierre =
                jdbc.queryForObject(
                        "SELECT fecha_cierre FROM evaluacion_subprueba WHERE evaluacion_id = ?",
                        java.sql.Timestamp.class,
                        id);
        enviar("{\"palabras\":[{\"ordenEntrada\":1,\"texto\":\"tardia\"}]}")
                .andExpect(status().isConflict());
        assertThat(
                        jdbc.queryForObject(
                                "SELECT fecha_cierre FROM evaluacion_subprueba WHERE evaluacion_id = ?",
                                java.sql.Timestamp.class,
                                id))
                .isEqualTo(cierre);
        assertThat(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM respuesta_evaluacion WHERE evaluacion_id = ?",
                                Long.class,
                                id))
                .isZero();
    }
}
