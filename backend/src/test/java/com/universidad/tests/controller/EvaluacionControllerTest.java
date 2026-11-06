package com.universidad.tests.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universidad.tests.application.service.MotorSubpruebasService;
import com.universidad.tests.domain.exception.RespuestaInvalidaException;
import com.universidad.tests.domain.exception.SubpruebaFueraDeOrdenException;
import com.universidad.tests.domain.exception.TiempoAgotadoException;
import com.universidad.tests.dto.AvanceSubpruebaResponseDto;
import com.universidad.tests.dto.EvaluacionResponseDto;
import com.universidad.tests.dto.PreguntasSubpruebaResponseDto;
import com.universidad.tests.dto.RegistrarRespuestaRequestDto;
import com.universidad.tests.dto.SubpruebaResponseDto;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EvaluacionController.class)
class EvaluacionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private MotorSubpruebasService motorSubpruebasService;

    @Test
    @DisplayName("E4: GET /api/v1/evaluaciones/{id} responde 200 con estado de subpruebas")
    void consultarEvaluacion_retorna200() throws Exception {
        Instant ahora = Instant.parse("2026-11-06T10:00:00Z");
        SubpruebaResponseDto sub =
                new SubpruebaResponseDto(
                        "VOC1",
                        "Vocabulario 1",
                        1,
                        300,
                        "ACTIVA",
                        285L,
                        ahora,
                        ahora.plusSeconds(300),
                        null,
                        true);
        EvaluacionResponseDto eval =
                new EvaluacionResponseDto("EV-01", "SUJETO-01", "EN_PROGRESO", List.of(sub));

        when(motorSubpruebasService.consultarEvaluacion("EV-01")).thenReturn(eval);

        mockMvc.perform(get("/api/v1/evaluaciones/EV-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("EV-01"))
                .andExpect(jsonPath("$.subpruebas[0].codigo").value("VOC1"))
                .andExpect(jsonPath("$.subpruebas[0].estado").value("ACTIVA"))
                .andExpect(jsonPath("$.subpruebas[0].segundosRestantes").value(285));
    }

    @Test
    @DisplayName(
            "E5: POST /api/v1/evaluaciones/{id}/subpruebas/{cod}/iniciar responde 200 con inicio oficial")
    void iniciarSubprueba_retorna200() throws Exception {
        Instant ahora = Instant.parse("2026-11-06T10:00:00Z");
        SubpruebaResponseDto sub =
                new SubpruebaResponseDto(
                        "VOC1",
                        "Vocabulario 1",
                        1,
                        300,
                        "ACTIVA",
                        300L,
                        ahora,
                        ahora.plusSeconds(300),
                        null,
                        true);

        when(motorSubpruebasService.iniciarSubprueba("EV-01", "VOC1")).thenReturn(sub);

        mockMvc.perform(post("/api/v1/evaluaciones/EV-01/subpruebas/VOC1/iniciar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("VOC1"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.segundosRestantes").value(300));
    }

    @Test
    @DisplayName("E5: Rechazo por orden responde 409 SUBPRUEBA_FUERA_DE_ORDEN con cuerpo estándar")
    void iniciarSubprueba_fueraDeOrden_retorna409() throws Exception {
        when(motorSubpruebasService.iniciarSubprueba("EV-01", "VOC2"))
                .thenThrow(
                        new SubpruebaFueraDeOrdenException(
                                "SUBPRUEBA_FUERA_DE_ORDEN",
                                "No se permite iniciar la subprueba VOC2 sin finalizar VOC1."));

        mockMvc.perform(post("/api/v1/evaluaciones/EV-01/subpruebas/VOC2/iniciar"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.codigo").value("SUBPRUEBA_FUERA_DE_ORDEN"))
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(
                        jsonPath("$.ruta")
                                .value("/api/v1/evaluaciones/EV-01/subpruebas/VOC2/iniciar"));
    }

    @Test
    @DisplayName("E6: GET /api/v1/evaluaciones/{id}/subpruebas/{cod}/preguntas responde 200")
    void consultarPreguntas_retorna200() throws Exception {
        PreguntasSubpruebaResponseDto dto =
                new PreguntasSubpruebaResponseDto("VOC1", 290L, true, List.of());

        when(motorSubpruebasService.consultarPreguntas("EV-01", "VOC1")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/evaluaciones/EV-01/subpruebas/VOC1/preguntas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("VOC1"))
                .andExpect(jsonPath("$.segundosRestantes").value(290));
    }

    @Test
    @DisplayName("E7: PUT /api/v1/evaluaciones/{id}/subpruebas/{cod}/respuestas responde 200")
    void registrarRespuesta_retorna200() throws Exception {
        RegistrarRespuestaRequestDto request =
                new RegistrarRespuestaRequestDto("P-01", "OPT-A", null, 1);
        AvanceSubpruebaResponseDto response =
                new AvanceSubpruebaResponseDto("EV-01", "VOC1", "P-01", 1, 10, true);

        when(motorSubpruebasService.registrarRespuesta(eq("EV-01"), eq("VOC1"), any()))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/evaluaciones/EV-01/subpruebas/VOC1/respuestas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guardado").value(true))
                .andExpect(jsonPath("$.totalRespondidas").value(1));
    }

    @Test
    @DisplayName("E7: Tiempo agotado responde 409 TIEMPO_AGOTADO")
    void registrarRespuesta_tiempoAgotado_retorna409() throws Exception {
        RegistrarRespuestaRequestDto request =
                new RegistrarRespuestaRequestDto("P-01", "OPT-A", null, 1);

        when(motorSubpruebasService.registrarRespuesta(eq("EV-01"), eq("VOC1"), any()))
                .thenThrow(new TiempoAgotadoException("El tiempo límite ha finalizado."));

        mockMvc.perform(
                        put("/api/v1/evaluaciones/EV-01/subpruebas/VOC1/respuestas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TIEMPO_AGOTADO"));
    }

    @Test
    @DisplayName("E7: Opción inválida responde 422 RESPUESTA_INVALIDA")
    void registrarRespuesta_opcionInvalida_retorna422() throws Exception {
        RegistrarRespuestaRequestDto request =
                new RegistrarRespuestaRequestDto("P-01", "OPT-AJENA", null, 1);

        when(motorSubpruebasService.registrarRespuesta(eq("EV-01"), eq("VOC1"), any()))
                .thenThrow(new RespuestaInvalidaException("Opción no pertenece a la pregunta."));

        mockMvc.perform(
                        put("/api/v1/evaluaciones/EV-01/subpruebas/VOC1/respuestas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("RESPUESTA_INVALIDA"));
    }

    @Test
    @DisplayName("E7: Solicitud sin preguntaId responde 400 SOLICITUD_INVALIDA con detalles")
    void registrarRespuesta_solicitudInvalida_retorna400() throws Exception {
        RegistrarRespuestaRequestDto request =
                new RegistrarRespuestaRequestDto("", "OPT-A", null, 1);

        mockMvc.perform(
                        put("/api/v1/evaluaciones/EV-01/subpruebas/VOC1/respuestas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"))
                .andExpect(jsonPath("$.detalles").isArray());
    }
}
