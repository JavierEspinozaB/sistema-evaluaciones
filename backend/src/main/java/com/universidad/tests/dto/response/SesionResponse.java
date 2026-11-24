package com.universidad.tests.dto.response;

import java.time.Instant;
import java.util.List;

/** Salida E14; la vigencia y los permisos los proporciona el flujo de acceso autorizado. */
public record SesionResponse(
        String token, Instant expiraEn, String rolActivo, List<String> permisos) {

    public SesionResponse {
        permisos = permisos == null ? null : List.copyOf(permisos);
    }

    @Override
    public String toString() {
        return "SesionResponse[token protegido]";
    }
}
