package com.universidad.tests.dto;

import java.util.List;

public record PreguntaResponseDto(
        Long id,
        int numero,
        String enunciado,
        String tipoRespuesta,
        @com.fasterxml.jackson.annotation.JsonProperty("recursoUrl") String recursoMultimedia,
        List<OpcionResponseDto> opciones,
        String respuestaActual) {}
