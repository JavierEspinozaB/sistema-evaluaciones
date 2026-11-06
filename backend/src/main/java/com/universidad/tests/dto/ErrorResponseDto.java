package com.universidad.tests.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDto(
        Instant timestamp,
        int status,
        String codigo,
        String mensaje,
        String ruta,
        List<DetalleErrorDto> detalles) {}
