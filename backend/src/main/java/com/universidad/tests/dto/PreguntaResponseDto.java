package com.universidad.tests.dto;

import java.util.List;

public record PreguntaResponseDto(
        String id,
        int numero,
        String enunciado,
        String tipoRespuesta,
        String recursoMultimedia,
        List<OpcionResponseDto> opciones,
        String respuestaActual) {}

