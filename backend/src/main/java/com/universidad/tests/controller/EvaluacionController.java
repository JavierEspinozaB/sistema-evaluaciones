package com.universidad.tests.controller;

import com.universidad.tests.application.service.ConsultaEvaluacionService;
import com.universidad.tests.dto.response.EvaluacionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/evaluaciones")
public class EvaluacionController {

    private final ConsultaEvaluacionService consultaService;

    public EvaluacionController(ConsultaEvaluacionService consultaService) {
        this.consultaService = consultaService;
    }

    // E4
    @GetMapping("/{evaluacionId}")
    public EvaluacionResponse consultarEvaluacion(@PathVariable Long evaluacionId) {
        return consultaService.consultar(evaluacionId);
    }
}
