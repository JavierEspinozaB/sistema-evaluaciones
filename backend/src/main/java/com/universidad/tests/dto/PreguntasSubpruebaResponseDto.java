package com.universidad.tests.dto;

import java.util.List;

public record PreguntasSubpruebaResponseDto(
        String codigo,
        Long segundosRestantes,
        boolean permitirRetroceso,
        List<PreguntaResponseDto> preguntas,
        List<PalabraFvDto> palabras,
        String letraInicial,
        Integer maximoEntradas) {
    public PreguntasSubpruebaResponseDto(
            String codigo,
            Long segundosRestantes,
            boolean permitirRetroceso,
            List<PreguntaResponseDto> preguntas) {
        this(codigo, segundosRestantes, permitirRetroceso, preguntas, List.of(), null, null);
    }
}
