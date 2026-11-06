package com.universidad.tests.dto;

import java.time.Instant;

public record SubpruebaResponseDto(
        String codigo,
        String nombre,
        int orden,
        int duracionSegundos,
        String estado,
        Long segundosRestantes,
        Instant inicio,
        Instant finLimite,
        String instrucciones,
        boolean permitirRetroceso) {}
