package com.universidad.tests.domain.model;

import java.time.Instant;

/** idSubprueba es la clave idempotente que el evaluador debe respetar en cada reintento. */
public record SolicitudCalculoSubprueba(
        Long idSubprueba,
        String evaluacionId,
        String codigoTest,
        String codigoSubprueba,
        Instant fechaCierre) {}
