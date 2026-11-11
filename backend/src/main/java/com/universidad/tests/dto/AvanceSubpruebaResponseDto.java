package com.universidad.tests.dto;

public record AvanceSubpruebaResponseDto(
        String evaluacionId,
        String subpruebaCodigo,
        String preguntaId,
        long totalRespondidas,
        int totalPreguntas,
        boolean guardado,
        String codigo,
        int registradas,
        Long segundosRestantes) {
    public AvanceSubpruebaResponseDto(
            String evaluacionId,
            String subpruebaCodigo,
            String preguntaId,
            long totalRespondidas,
            int totalPreguntas,
            boolean guardado) {
        this(
                evaluacionId,
                subpruebaCodigo,
                preguntaId,
                totalRespondidas,
                totalPreguntas,
                guardado,
                subpruebaCodigo,
                1,
                null);
    }
}
