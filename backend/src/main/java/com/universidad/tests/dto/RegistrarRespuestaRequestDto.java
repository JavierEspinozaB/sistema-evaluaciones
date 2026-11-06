package com.universidad.tests.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrarRespuestaRequestDto(
        @NotBlank(message = "preguntaId es obligatorio") String preguntaId,
        String opcionId,
        String valorTexto,
        Integer ordenEntrada) {}
