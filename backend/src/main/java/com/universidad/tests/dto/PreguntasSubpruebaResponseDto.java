package com.universidad.tests.dto;

import java.util.List;

public record PreguntasSubpruebaResponseDto(
        String codigo,
        Long segundosRestantes,
        boolean permitirRetroceso,
        List<PreguntaResponseDto> preguntas) {}

