package com.universidad.tests.dto;

public record ItemRespuestaDto(
        String preguntaId,
        String opcion,
        String opcionId,
        String valorTexto,
        Integer ordenEntrada) {

    public ItemRespuestaDto(String preguntaId, String opcion) {
        this(preguntaId, opcion, opcion, null, 1);
    }
}
