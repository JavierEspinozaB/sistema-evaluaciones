package com.universidad.tests.controller;

import com.universidad.tests.application.service.MotorSubpruebasService;
import com.universidad.tests.dto.AvanceSubpruebaResponseDto;
import com.universidad.tests.dto.EvaluacionResponseDto;
import com.universidad.tests.dto.PreguntasSubpruebaResponseDto;
import com.universidad.tests.dto.RegistrarRespuestaRequestDto;
import com.universidad.tests.dto.SubpruebaResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/evaluaciones")
public class EvaluacionController {

    private final MotorSubpruebasService motorSubpruebasService;

    public EvaluacionController(MotorSubpruebasService motorSubpruebasService) {
        this.motorSubpruebasService = motorSubpruebasService;
    }

    @GetMapping("/{evaluacionId}")
    public ResponseEntity<EvaluacionResponseDto> consultarEvaluacion(
            @PathVariable String evaluacionId) {
        EvaluacionResponseDto response = motorSubpruebasService.consultarEvaluacion(evaluacionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{evaluacionId}/subpruebas/{codigo}/iniciar")
    public ResponseEntity<SubpruebaResponseDto> iniciarSubprueba(
            @PathVariable String evaluacionId, @PathVariable String codigo) {
        SubpruebaResponseDto response =
                motorSubpruebasService.iniciarSubprueba(evaluacionId, codigo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{evaluacionId}/subpruebas/{codigo}/preguntas")
    public ResponseEntity<PreguntasSubpruebaResponseDto> consultarPreguntas(
            @PathVariable String evaluacionId, @PathVariable String codigo) {
        PreguntasSubpruebaResponseDto response =
                motorSubpruebasService.consultarPreguntas(evaluacionId, codigo);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{evaluacionId}/subpruebas/{codigo}/respuestas")
    public ResponseEntity<AvanceSubpruebaResponseDto> registrarRespuesta(
            @PathVariable String evaluacionId,
            @PathVariable String codigo,
            @Valid @RequestBody RegistrarRespuestaRequestDto request) {
        AvanceSubpruebaResponseDto response =
                motorSubpruebasService.registrarRespuesta(evaluacionId, codigo, request);
        return ResponseEntity.ok(response);
    }
}
