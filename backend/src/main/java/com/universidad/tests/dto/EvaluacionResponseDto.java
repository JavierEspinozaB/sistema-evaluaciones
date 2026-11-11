package com.universidad.tests.dto;

import java.util.List;

public record EvaluacionResponseDto(
        String id,
        String sujetoId,
        String estado,
        List<SubpruebaResponseDto> subpruebas,
        String testId,
        String periodoId,
        String versionFormularioId,
        java.time.Instant fechaInicio,
        java.time.Instant fechaFin,
        SubpruebaResponseDto subpruebaActiva) {
    public EvaluacionResponseDto(
            String id, String sujetoId, String estado, List<SubpruebaResponseDto> subpruebas) {
        this(
                id,
                sujetoId,
                estado,
                subpruebas,
                null,
                null,
                null,
                null,
                null,
                subpruebas.stream()
                        .filter(s -> "ACTIVA".equals(s.estado()))
                        .findFirst()
                        .orElse(null));
    }
}
