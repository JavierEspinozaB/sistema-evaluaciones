package com.universidad.tests;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Adaptador real sin GEN-06: no confía en encabezados y nunca permite acceso anónimo. */
@SpringBootTest(properties = "motor.temporizador.habilitado=false")
@AutoConfigureMockMvc
class AccesoMotorIntegrationTest {
    @Autowired MockMvc mvc;

    @Test
    void endpointsSinSesionDan401InclusoConCabecerasFalsas() throws Exception {
        var base = "/api/v1/evaluaciones/NO-EXISTE/subpruebas/VOC1/";
        mvc.perform(post(base + "iniciar").header("X-User-Id", "admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
        mvc.perform(get(base + "preguntas").header("Authorization", "Bearer inventado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
        mvc.perform(
                        put(base + "respuestas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"respuestas\":[]}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
    }

    @Test
    void identidadSinPermisoDa403SinRevelarSiExisteLaEvaluacion() throws Exception {
        var base = "/api/v1/evaluaciones/NO-EXISTE/subpruebas/VOC1/";
        mvc.perform(post(base + "iniciar").principal(() -> "usuario-ficticio"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("SIN_PERMISO"));
        mvc.perform(get(base + "preguntas").principal(() -> "usuario-ficticio"))
                .andExpect(status().isForbidden());
        mvc.perform(
                        put(base + "respuestas")
                                .principal(() -> "usuario-ficticio")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"respuestas\":[]}"))
                .andExpect(status().isForbidden());
    }
}
