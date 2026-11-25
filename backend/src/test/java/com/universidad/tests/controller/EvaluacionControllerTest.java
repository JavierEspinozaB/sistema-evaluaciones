package com.universidad.tests.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.universidad.tests.application.service.AutenticacionService;
import com.universidad.tests.application.service.ConsultaEvaluacionService;
import com.universidad.tests.application.service.IdentidadSesion;
import com.universidad.tests.domain.exception.EvaluacionNoEncontradaException;
import com.universidad.tests.dto.response.EvaluacionResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EvaluacionController.class)
class EvaluacionControllerTest {

    // Token ficticio; la validacion real se prueba en AutenticacionServiceTest
    private static final String BEARER = "Bearer token-ficticio";

    @Autowired private MockMvc mvc;

    @MockitoBean private ConsultaEvaluacionService consultaService;

    @MockitoBean private AutenticacionService autenticacion;

    @BeforeEach
    void sesionValida() {
        when(autenticacion.validar(anyString())).thenReturn(Optional.empty());
        when(autenticacion.validar("token-ficticio"))
                .thenReturn(Optional.of(new IdentidadSesion(1L, 1L, "SUJETO", Set.of())));
    }

    @Test
    void e4DevuelveElEstadoComoPideElContrato() throws Exception {
        Instant fin = Instant.parse("2026-10-30T15:42:10Z");
        when(consultaService.consultar(15L))
                .thenReturn(
                        new EvaluacionResponse(
                                15L,
                                1L,
                                25L,
                                3L,
                                10L,
                                "EN_CURSO",
                                Instant.parse("2026-10-30T15:30:00Z"),
                                null,
                                new EvaluacionResponse.SubpruebaActiva("VOC2", fin, 214),
                                List.of(
                                        new EvaluacionResponse.SubpruebaEstado(
                                                "VOC1", "CERRADA", fin),
                                        new EvaluacionResponse.SubpruebaEstado(
                                                "VOC2", "ACTIVA", null))));

        mvc.perform(get("/api/v1/evaluaciones/15").header("Authorization", BEARER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.testId").value(1))
                .andExpect(jsonPath("$.sujetoId").value(25))
                .andExpect(jsonPath("$.periodoId").value(3))
                .andExpect(jsonPath("$.versionFormularioId").value(10))
                .andExpect(jsonPath("$.estado").value("EN_CURSO"))
                .andExpect(jsonPath("$.fechaFin").isEmpty())
                .andExpect(jsonPath("$.subpruebaActiva.codigo").value("VOC2"))
                .andExpect(jsonPath("$.subpruebaActiva.segundosRestantes").value(214))
                .andExpect(jsonPath("$.subpruebas[0].estado").value("CERRADA"))
                .andExpect(jsonPath("$.subpruebas[1].cerradaEn").isEmpty());
    }

    @Test
    void e4Responde404ConElFormatoComunDeError() throws Exception {
        when(consultaService.consultar(99L)).thenThrow(new EvaluacionNoEncontradaException(99L));

        mvc.perform(get("/api/v1/evaluaciones/99").header("Authorization", BEARER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.codigo").value("EVALUACION_NO_ENCONTRADA"))
                .andExpect(jsonPath("$.ruta").value("/api/v1/evaluaciones/99"));
    }
}
