package com.universidad.tests.dto;

import java.util.List;

public record EvaluacionResponseDto(
        String id, String sujetoId, String estado, List<SubpruebaResponseDto> subpruebas) {}
