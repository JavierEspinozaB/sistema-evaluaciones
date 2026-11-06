package com.universidad.tests.dto;

public record AvanceSubpruebaResponseDto(
        String evaluacionId,
        String subpruebaCodigo,
        String preguntaId,
        long totalRespondidas,
        int totalPreguntas,
        boolean guardado) {}
